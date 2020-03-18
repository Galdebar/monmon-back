package lt.galdebar.monmonmvc.service;


import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingItemDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonmvc.service.exceptions.shoppingitem.ShoppingItemNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
//@RequiredArgsConstructor
public class ShoppingItemService {

    @Autowired
    private ShoppingItemRepo shoppingItemRepo;

    @Autowired
    private UserService userService;

    @Autowired
    private ShoppingItemCategoryService shoppingItemCategoryService;

    public ShoppingItemDTO addItem(ShoppingItemDTO shoppingItemDTO) {
        if (shoppingItemDTO.getItemCategory().trim().isEmpty()) {
            ShoppingCategoryDTO foundCategory = shoppingItemCategoryService.findCategoryByKeyword(
                    new ShoppingKeywordDTO("", shoppingItemDTO.getItemName())
            );
            shoppingItemDTO.setItemCategory(foundCategory.getCategoryName());
        }
        ShoppingItemDAO returnedItem = shoppingItemRepo.insert(dtoToDao(shoppingItemDTO));
        return daoToDto(returnedItem);
    }

    public Optional<ShoppingItemDAO> getItemById(String id) {
        return shoppingItemRepo.findById(id);
    }

    public List<ShoppingItemDTO> getItemsByCategory(String requestedCategory) {
        List<ShoppingItemDAO> foundItems = shoppingItemRepo.findByItemCategory(requestedCategory);
        return daosToDtos(foundItems);
//        return shoppingItemRepo.findByItemCategoryAndUsers(requestedCategory, getCurrentUserAndConnectedUsers());
    }

    public List<ShoppingItemDTO> getAll() {
        return daosToDtos(shoppingItemRepo.findByUsersIn(getCurrentUserAndConnectedUsers()));
    }


    public ShoppingItemDTO updateItem(ShoppingItemDTO shoppingItemDTO) throws ShoppingItemNotFound {
        if(!validateShoppingItemDTO(shoppingItemDTO)){
            throw new ShoppingItemNotFound();
        }
        if(!shoppingItemRepo.existsById(shoppingItemDTO.getId())){
            throw new ShoppingItemNotFound();
        }
        ShoppingItemDAO result = shoppingItemRepo.save(dtoToDao(shoppingItemDTO));
        return daoToDto(result);
    }

    public List<ShoppingItemDTO> updateItems(List<ShoppingItemDTO> shoppingItemDTOS) throws ShoppingItemNotFound {
        for(ShoppingItemDTO item: shoppingItemDTOS){
            if(!validateShoppingItemDTO(item)){
                throw new ShoppingItemNotFound();
            }
            if(!shoppingItemRepo.existsById(item.getId())){
                throw new ShoppingItemNotFound();
            }
        }
        List<ShoppingItemDAO> updatedItems = shoppingItemRepo.saveAll(dtosToDaos(shoppingItemDTOS));
        return daosToDtos(updatedItems);
    }

    public void deleteItem(ShoppingItemDTO shoppingItemDTO) throws ShoppingItemNotFound {
        if(!validateShoppingItemDTO(shoppingItemDTO)){
            throw new ShoppingItemNotFound();
        }
        if(!shoppingItemRepo.existsById(shoppingItemDTO.getId())){
            throw new ShoppingItemNotFound();
        }
        shoppingItemRepo.delete(dtoToDao(shoppingItemDTO));
    }

    public void deleteItems(List<ShoppingItemDTO> shoppingItemDTOList) throws ShoppingItemNotFound {
        for(ShoppingItemDTO item: shoppingItemDTOList){
            if(!validateShoppingItemDTO(item)){
                throw new ShoppingItemNotFound();
            }
            if(!shoppingItemRepo.existsById(item.getId())){
                throw new ShoppingItemNotFound();
            }
        }
        shoppingItemRepo.deleteAll(dtosToDaos(shoppingItemDTOList));
    }

    private ShoppingItemDAO dtoToDao(ShoppingItemDTO shoppingItemDTO) {
        ShoppingItemDAO shoppingItemDAO = new ShoppingItemDAO();
        shoppingItemDAO.id = shoppingItemDTO.getId();
        shoppingItemDAO.itemName = shoppingItemDTO.getItemName();
        shoppingItemDAO.itemCategory = shoppingItemDTO.getItemCategory();
        shoppingItemDAO.quantity = shoppingItemDTO.getQuantity();
        shoppingItemDAO.comment = shoppingItemDTO.getComment();
        shoppingItemDAO.isInCart = shoppingItemDTO.isInCart();
        shoppingItemDAO.users.add(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
        return shoppingItemDAO;
    }

    private List<ShoppingItemDAO> dtosToDaos(List<ShoppingItemDTO> shoppingItemDTOList) {
        List<ShoppingItemDAO> shoppingItemDAOList = new ArrayList<>();
        shoppingItemDTOList.forEach(dto -> shoppingItemDAOList.add(dtoToDao(dto)));
        return shoppingItemDAOList;
    }

    private ShoppingItemDTO daoToDto(ShoppingItemDAO shoppingItemDAO) {
        return new ShoppingItemDTO(
                shoppingItemDAO.id,
                shoppingItemDAO.itemName,
                shoppingItemDAO.itemCategory,
                shoppingItemDAO.quantity,
                shoppingItemDAO.comment,
                shoppingItemDAO.isInCart
        );
    }

    private List<ShoppingItemDTO> daosToDtos(List<ShoppingItemDAO> shoppingItemDAOList) {
        List<ShoppingItemDTO> shoppingItemDTOList = new ArrayList<>();
        shoppingItemDAOList.forEach(dao -> shoppingItemDTOList.add(daoToDto(dao)));
        return shoppingItemDTOList;
    }

    private List<String> getCurrentUserAndConnectedUsers() {
        List<String> users = userService.getLinkedUsers();
        users.add(SecurityContextHolder.getContext().getAuthentication().getName());
        System.out.println(users);
        return users;
    }

    private boolean validateShoppingItemDTO(ShoppingItemDTO shoppingItemDTO) {
        if(shoppingItemDTO == null){
            return false;
        }
        if(shoppingItemDTO.getId() == null){
            return false;
        }
        if(shoppingItemDTO.getId().trim().isEmpty()){
            return false;
        }

        return true;
    }
}
