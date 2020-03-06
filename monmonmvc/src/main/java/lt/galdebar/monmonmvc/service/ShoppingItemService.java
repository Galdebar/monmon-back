package lt.galdebar.monmonmvc.service;


import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingItemDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.persistence.repositories.ShoppingItemRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
    private ShoppingItemCategoryService shoppingItemCategoryService;

    public ShoppingItemDTO addItem(ShoppingItemDTO shoppingItemDTO) {
        ShoppingCategoryDTO foundCategory = shoppingItemCategoryService.findCategoryByKeyword(
                new ShoppingKeywordDTO("", shoppingItemDTO.getItemName())
        );
        shoppingItemDTO.setItemCategory(foundCategory.getCategoryName());
        ShoppingItemDAO returnedItem = shoppingItemRepo.insert(dtoToDao(shoppingItemDTO));
        return daoToDto(returnedItem);
    }

    public Optional<ShoppingItemDAO> getItemById(String id) {
        return shoppingItemRepo.findById(id);
    }

    public List<ShoppingItemDAO> getItemsByCategory(String requestedCategory) {
//        return shoppingItemRepo.findByItemCategory(requestedCategory);
        return shoppingItemRepo.findByItemCategoryAndUsers(requestedCategory, getCurrentUser());
    }

    public List<ShoppingItemDTO> getAll() {
        return daosToDtos(shoppingItemRepo.findByUsers(getCurrentUser()));
    }


    public ShoppingItemDTO updateItem(ShoppingItemDTO shoppingItemDTO) {
        ShoppingItemDAO result = shoppingItemRepo.save(dtoToDao(shoppingItemDTO));
        return daoToDto(result);
    }

    public List<ShoppingItemDTO> updateItems(List<ShoppingItemDTO> shoppingItemDTOS) {
        List<ShoppingItemDAO> updatedItems = shoppingItemRepo.saveAll(dtosToDaos(shoppingItemDTOS));
        return daosToDtos(updatedItems);
    }

    public void deleteItem(ShoppingItemDTO shoppingItemDTO) {
        shoppingItemRepo.delete(dtoToDao(shoppingItemDTO));
    }

    public void deleteItems(List<ShoppingItemDTO> shoppingItemDTOList) {
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
        return  shoppingItemDAO;
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

    private String getCurrentUser(){
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
