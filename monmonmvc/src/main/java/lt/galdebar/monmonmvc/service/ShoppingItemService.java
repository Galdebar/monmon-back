package lt.galdebar.monmonmvc.service;


import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingCategoryEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingItemEntity;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import lt.galdebar.monmonmvc.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonmvc.service.exceptions.shoppingitem.ShoppingItemNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class ShoppingItemService {
    @Autowired
    private ShoppingItemRepo shoppingItemRepo;
    @Autowired
    private UserService userService;
    @Autowired
    private ShoppingItemCategoryService shoppingItemCategoryService;

    public List<ShoppingItemDTO> getItemsByCategory(String requestedCategory) {
        List<ShoppingItemEntity> foundItems = shoppingItemRepo.findByItemCategory(requestedCategory);
        return entitiesToDtos(foundItems);
    }

    public List<ShoppingItemDTO> getAll() {
        return entitiesToDtos(shoppingItemRepo.findByUsersIn(getCurrentUserAndConnectedUsers()));
    }

    public List<ShoppingItemDTO> getAll(UserDTO userDTO) {
        List<ShoppingItemEntity> foundItems = shoppingItemRepo.findByUsersIn(
                Collections.singletonList(userDTO.getUserEmail())
        );

        return entitiesToDtos(foundItems);
    }

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

        shoppingItemDTO = addUsersIfEmpty(shoppingItemDTO);

        ShoppingItemEntity returnedItem = shoppingItemRepo.insert(dtoToEntity(shoppingItemDTO));
        return entityToDto(returnedItem);
    }


    @Transactional
    public ShoppingItemDTO updateItem(ShoppingItemDTO shoppingItemDTO) throws ShoppingItemNotFound {
        if (!validateShoppingItemDTO(shoppingItemDTO)) {
            throw new ShoppingItemNotFound(shoppingItemDTO.getId());
        }
        if (!shoppingItemRepo.existsById(shoppingItemDTO.getId())) {
            throw new ShoppingItemNotFound(shoppingItemDTO.getId());
        }

        shoppingItemDTO = addUsersIfEmpty(shoppingItemDTO);

        ShoppingItemEntity result = shoppingItemRepo.save(dtoToEntity(shoppingItemDTO));
        return entityToDto(result);
    }

    @Transactional
    public List<ShoppingItemDTO> updateItems(List<ShoppingItemDTO> shoppingItemDTOS) throws ShoppingItemNotFound {
        for (ShoppingItemDTO item : shoppingItemDTOS) {
            if (!validateShoppingItemDTO(item)) {
                throw new ShoppingItemNotFound(item.getId());
            }
            if (!shoppingItemRepo.existsById(item.getId())) {
                throw new ShoppingItemNotFound(item.getId());
            }
            addUsersIfEmpty(item);
        }
        List<ShoppingItemEntity> updatedItems = shoppingItemRepo.saveAll(dtosToEntities(shoppingItemDTOS));
        return entitiesToDtos(updatedItems);
    }

    @Transactional
    public void deleteItem(ShoppingItemDTO shoppingItemDTO) throws ShoppingItemNotFound {
        if (!validateShoppingItemDTO(shoppingItemDTO)) {
            throw new ShoppingItemNotFound(shoppingItemDTO.getId());
        }
        if (!shoppingItemRepo.existsById(shoppingItemDTO.getId())) {
            throw new ShoppingItemNotFound(shoppingItemDTO.getId());
        }
        shoppingItemRepo.delete(dtoToEntity(shoppingItemDTO));
    }

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
        shoppingItemRepo.deleteAll(dtosToEntities(shoppingItemDTOList));
    }

    private ShoppingItemEntity dtoToEntity(ShoppingItemDTO shoppingItemDTO) {
        ShoppingItemEntity shoppingItemEntity = new ShoppingItemEntity();
        shoppingItemEntity.id = shoppingItemDTO.getId();
        shoppingItemEntity.itemName = shoppingItemDTO.getItemName();
        shoppingItemEntity.itemCategory = shoppingItemDTO.getItemCategory();
        shoppingItemEntity.quantity = shoppingItemDTO.getQuantity();
        shoppingItemEntity.comment = shoppingItemDTO.getComment();
        shoppingItemEntity.isInCart = shoppingItemDTO.isInCart();
        shoppingItemEntity.users.addAll(shoppingItemDTO.getUsers());
        return shoppingItemEntity;
    }

    private List<ShoppingItemEntity> dtosToEntities(List<ShoppingItemDTO> shoppingItemDTOList) {
        List<ShoppingItemEntity> shoppingItemEntityList = new ArrayList<>();
        shoppingItemDTOList.forEach(dto -> shoppingItemEntityList.add(dtoToEntity(dto)));
        return shoppingItemEntityList;
    }

    private ShoppingItemDTO entityToDto(ShoppingItemEntity shoppingItemEntity) {
        return new ShoppingItemDTO(
                shoppingItemEntity.id,
                shoppingItemEntity.itemName,
                shoppingItemEntity.itemCategory,
                shoppingItemEntity.quantity,
                shoppingItemEntity.comment,
                shoppingItemEntity.isInCart,
                shoppingItemEntity.users
        );
    }

    private List<ShoppingItemDTO> entitiesToDtos(List<ShoppingItemEntity> shoppingItemEntityList) {
        List<ShoppingItemDTO> shoppingItemDTOList = new ArrayList<>();
        shoppingItemEntityList.forEach(dao -> shoppingItemDTOList.add(entityToDto(dao)));
        return shoppingItemDTOList;
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

    private ShoppingItemDTO addUsersIfEmpty(ShoppingItemDTO shoppingItemDTO){
        if (shoppingItemDTO.users.size() == 0) {
            shoppingItemDTO.users.add(
                    SecurityContextHolder.getContext().getAuthentication().getName()
            );
            shoppingItemDTO.users.addAll(userService.getLinkedUsers());
        }
        return shoppingItemDTO;
    }
}
