package lt.galdebar.monmonmvc.service;


import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingItemEntity;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonmvc.service.adapters.ShoppingItemAdapter;
import lt.galdebar.monmonmvc.service.exceptions.shoppingitem.ShoppingItemNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

/**
 * Handles checks and verifications before shopping item CRUD operations.
 */
@Service
public class ShoppingItemService {
    private ShoppingItemAdapter adapter = new ShoppingItemAdapter();

    @Autowired
    private ShoppingItemRepo shoppingItemRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private ShoppingItemCategoryService shoppingItemCategoryService;

    /**
     * Gets items by category.
     *
     * @param requestedCategory the requested category
     * @return Shopping Item list. Empty if nothing found.
     */
    public List<ShoppingItemDTO> getItemsByCategory(String requestedCategory) {
        List<ShoppingItemEntity> foundItems = shoppingItemRepo.findByItemCategory(requestedCategory);
        return adapter.bToA(foundItems);
    }

    /**
     * Gets all shopping items for current user and linked users (if there are any).
     *
     * @return List of Shopping items. Empty if nothing found.
     */
    public List<ShoppingItemDTO> getAll() {
//        return entitiesToDtos(shoppingItemRepo.findByUsersIn(getCurrentUserAndConnectedUsers()));
        return adapter.bToA(shoppingItemRepo.findByUsersIn(getCurrentUserAndConnectedUsers()));
    }

    /**
     * Gets all shopping items when given a user.
     *
     * @param userDTO the user dto. Should contain valid email adress.
     * @return List of Shopping Items. Empty if nothing found, or in any way incorrect email address given.
     */
    public List<ShoppingItemDTO> getAll(UserDTO userDTO) {
        List<ShoppingItemEntity> foundItems = shoppingItemRepo.findByUsersIn(
                Collections.singletonList(userDTO.getUserEmail())
        );

        return adapter.bToA(foundItems);
    }

    /**
     * Add a new Shopping item to DB. Assigns a category if there is none based on the item name.
     * If there's no valid category to be found, assigns "Uncategorized"
     *
     * @param shoppingItemDTO the shopping item dto
     * @return the saved Shopping item with assigned category and id.
     */
    @Transactional
    public ShoppingItemDTO addItem(ShoppingItemDTO shoppingItemDTO) {
        if (shoppingItemDTO.getItemCategory().trim().isEmpty()) {
            ShoppingCategoryDTO foundCategory = shoppingItemCategoryService.findCategoryByKeyword(
                    new ShoppingKeywordDTO("", shoppingItemDTO.getItemName())
            );
            shoppingItemDTO.setItemCategory(foundCategory.getCategoryName());
        }

        ShoppingCategoryDTO searchCategory = new ShoppingCategoryDTO();
        searchCategory.setCategoryName(shoppingItemDTO.getItemCategory());
        shoppingItemDTO.setItemCategory(
                shoppingItemCategoryService.searchCategory(
                        searchCategory
                ).getCategoryName()
        );

        ShoppingItemDTO itemToAdd = addUsersIfEmpty(shoppingItemDTO);

        ShoppingItemEntity returnedItem = shoppingItemRepo.insert(
                adapter.aToB(itemToAdd)
        );

        return adapter.bToA(returnedItem);
    }


    /**
     * Update item shopping item dto. Carries all fields over.
     * If fields are null or empty- item in DB will be updated accordingly.
     *
     * @param shoppingItemDTO the shopping item dto
     * @return the shopping item dto
     * @throws ShoppingItemNotFound the shopping item not found (invalid id)
     */
    @Transactional
    public ShoppingItemDTO updateItem(ShoppingItemDTO shoppingItemDTO) throws ShoppingItemNotFound {
        if (!validateShoppingItemDTO(shoppingItemDTO)) {
            throw new ShoppingItemNotFound(shoppingItemDTO.getId());
        }
        if (!shoppingItemRepo.existsById(shoppingItemDTO.getId())) {
            throw new ShoppingItemNotFound(shoppingItemDTO.getId());
        }

        ShoppingItemDTO itemToUpdate = addUsersIfEmpty(shoppingItemDTO);

        ShoppingItemEntity result = shoppingItemRepo.save(
                adapter.aToB(itemToUpdate)
        );

        return adapter.bToA(result);
    }

    /**
     * Update items list.
     *
     * @param shoppingItemDTOS the shopping item dtos
     * @return the list
     * @throws ShoppingItemNotFound Any shopping item not found.
     */
    @Transactional
    public List<ShoppingItemDTO> updateItems(List<ShoppingItemDTO> shoppingItemDTOS) throws ShoppingItemNotFound {
        List<ShoppingItemDTO> itemsToUpdate = new ArrayList<>();
        for (ShoppingItemDTO item : shoppingItemDTOS) {
            if (!validateShoppingItemDTO(item)) {
                throw new ShoppingItemNotFound(item.getId());
            }
            if (!shoppingItemRepo.existsById(item.getId())) {
                throw new ShoppingItemNotFound(item.getId());
            }
            itemsToUpdate.add(addUsersIfEmpty(item));
        }
        List<ShoppingItemEntity> updatedItems = shoppingItemRepo.saveAll(
                adapter.aToB(itemsToUpdate)
        );

        return adapter.bToA(updatedItems);
    }

    /**
     * Delete item.
     *
     * @param shoppingItemDTO the shopping item dto
     * @throws ShoppingItemNotFound the shopping item not found (invalid id).
     */
    @Transactional
    public void deleteItem(ShoppingItemDTO shoppingItemDTO) throws ShoppingItemNotFound {
        if (!validateShoppingItemDTO(shoppingItemDTO)) {
            throw new ShoppingItemNotFound(shoppingItemDTO.getId());
        }
        if (!shoppingItemRepo.existsById(shoppingItemDTO.getId())) {
            throw new ShoppingItemNotFound(shoppingItemDTO.getId());
        }
        shoppingItemRepo.delete(
                adapter.aToB(shoppingItemDTO)
        );
    }

    /**
     * Delete items.
     *
     * @param shoppingItemDTOList the shopping item dto list
     * @throws ShoppingItemNotFound any item not found.
     */
    @Transactional
    public void deleteItems(List<ShoppingItemDTO> shoppingItemDTOList) throws ShoppingItemNotFound {
        for (ShoppingItemDTO item : shoppingItemDTOList) {
            if (!validateShoppingItemDTO(item)) {
                throw new ShoppingItemNotFound(item.getId());
            }
            if (!shoppingItemRepo.existsById(item.getId())) {
                throw new ShoppingItemNotFound(item.getId());
            }
        }
        shoppingItemRepo.deleteAll(
                adapter.aToB(shoppingItemDTOList)
        );
    }

    private List<String> getCurrentUserAndConnectedUsers() {
        List<String> users = userService.getLinkedUsers();
        users.add(SecurityContextHolder.getContext().getAuthentication().getName());
        System.out.println(users);
        return users;
    }

    private boolean validateShoppingItemDTO(ShoppingItemDTO shoppingItemDTO) {
        if (shoppingItemDTO == null) {
            return false;
        }
        if (shoppingItemDTO.getId() == null) {
            return false;
        }
        if (shoppingItemDTO.getId().trim().isEmpty()) {
            return false;
        }

        return true;
    }

    private ShoppingItemDTO addUsersIfEmpty(ShoppingItemDTO shoppingItemDTO) {
        if(shoppingItemDTO.getUsers() == null){
            shoppingItemDTO.setUsers(new HashSet<>());
        }
        if (shoppingItemDTO.getUsers().size() == 0) {
            shoppingItemDTO.getUsers().add(
                    SecurityContextHolder.getContext().getAuthentication().getName()
            );
            shoppingItemDTO.getUsers().addAll(
                    userService.getLinkedUsers()
            );
        }
        return shoppingItemDTO;
    }
}
