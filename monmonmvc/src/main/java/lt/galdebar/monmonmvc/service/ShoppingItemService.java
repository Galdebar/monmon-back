package lt.galdebar.monmonmvc.service;


import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingItemDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.persistence.repositories.MongoDBRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ShoppingItemService {

    @Autowired
    private final MongoDBRepo mongoDBRepo;

    @Autowired
    private ShoppingItemCategoryService shoppingItemCategoryService;

    public ShoppingItemDTO addItem(ShoppingItemDTO shoppingItemDTO) {
        ShoppingCategoryDTO foundCategory = shoppingItemCategoryService.findCategoryByKeyword(
                new ShoppingKeywordDTO("",shoppingItemDTO.getItemName())
        );
        shoppingItemDTO.setItemCategory(foundCategory.getCategoryName());
        ShoppingItemDAO returnedItem = mongoDBRepo.insert(dtoToDao(shoppingItemDTO));
        return daoToDto(returnedItem);
    }

    public Optional<ShoppingItemDAO> getItemById(String id) {
        return mongoDBRepo.findById(id);
    }

    public ShoppingItemDAO getItemByName(String itemName) {
        return mongoDBRepo.findByItemName(itemName);
    }

    public List<ShoppingItemDAO> getItemsByCategory(String requestedCategory) {
        return mongoDBRepo.findByItemCategory(requestedCategory);
    }

    public List<ShoppingItemDTO> getAll() {
        return daosToDtos(mongoDBRepo.findAll());
    }


    public ShoppingItemDTO updateItem(ShoppingItemDTO shoppingItemDTO) {
        ShoppingItemDAO result = mongoDBRepo.save(dtoToDao(shoppingItemDTO));
        return daoToDto(result);
    }

    public List<ShoppingItemDTO> updateItems(List<ShoppingItemDTO> shoppingItemDTOS) {
        List<ShoppingItemDAO> updatedItems = mongoDBRepo.saveAll(dtosToDaos(shoppingItemDTOS));
        return daosToDtos(updatedItems);
    }

    public void deleteItem(ShoppingItemDTO shoppingItemDTO) {
        mongoDBRepo.delete(dtoToDao(shoppingItemDTO));
    }

    public void deleteItems(List<ShoppingItemDTO> shoppingItemDTOList) {
        mongoDBRepo.deleteAll(dtosToDaos(shoppingItemDTOList));
    }

    private ShoppingItemDAO dtoToDao(ShoppingItemDTO shoppingItemDTO) {
        return new ShoppingItemDAO(
                shoppingItemDTO.getId(),
                shoppingItemDTO.getItemName(),
                shoppingItemDTO.getItemCategory(),
                shoppingItemDTO.getQuantity(),
                shoppingItemDTO.getComment(),
                shoppingItemDTO.isInCart()
        );
    }

    private List<ShoppingItemDAO> dtosToDaos(List<ShoppingItemDTO> shoppingItemDTOList) {
        List<ShoppingItemDAO> shoppingItemDAOList = new ArrayList<>();
        shoppingItemDTOList.forEach(dto -> {
            shoppingItemDAOList.add(dtoToDao(dto));
        });
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
        shoppingItemDAOList.forEach(dao -> {
            shoppingItemDTOList.add(daoToDto(dao));
        });
        return shoppingItemDTOList;
    }
}
