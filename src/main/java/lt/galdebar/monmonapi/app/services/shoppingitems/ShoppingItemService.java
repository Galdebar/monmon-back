package lt.galdebar.monmonapi.app.services.shoppingitems;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemEntity;
import lt.galdebar.monmonapi.app.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.InvalidShoppingItemRequest;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.ItemNotFound;
import lt.galdebar.monmonapi.app.services.shoppinglists.ShoppingListService;
import lt.galdebar.monmonapi.categoriesparser.services.ShoppingItemCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingItemService {

    private final ShoppingItemRepo itemRepo;
    private final ShoppingListService listService;
    private final ShoppingItemCategoryService categoryService;

    public List<ShoppingItemDTO> getAll() {
        List<ShoppingItemEntity> foundItems = itemRepo.findByShoppingList(
                listService.getCurrentList()
        );
        return foundItems.stream().map(ShoppingItemEntity::getDTO).collect(Collectors.toList());
    }

    @Transactional
    public ShoppingItemDTO addItem(ShoppingItemDTO shoppingItemDTO) {
        shoppingItemDTO.checkIfValid();
        if(shoppingItemDTO.getItemCategory() == null || shoppingItemDTO.getItemCategory().trim().isEmpty()){
            shoppingItemDTO.setItemCategory(
                    categoryService.findCategoryByKeyword(shoppingItemDTO.getItemName()).getCategoryName()
            );
        }
        //find deal
        ShoppingItemEntity entityToSave = new ShoppingItemEntity(shoppingItemDTO);
        return itemRepo.save(entityToSave).getDTO();
    }

    @Transactional
    public ShoppingItemDTO updateItem(ShoppingItemDTO shoppingItemDTO) {
        if(shoppingItemDTO.getId() == null){
            throw new InvalidShoppingItemRequest("ID field cannot be empty");
        }

        Optional<ShoppingItemEntity> entityOptional = itemRepo.findById(shoppingItemDTO.getId());
        if (entityOptional.isEmpty() ||
                !entityOptional.get().getShoppingList().getId().equals(listService.getCurrentList().getId())) {
            throw new ItemNotFound("Could not find item with ID: " + shoppingItemDTO.getId());
        }
        ShoppingItemEntity itemToSave = new ShoppingItemEntity(shoppingItemDTO);

        return itemRepo.save(itemToSave).getDTO();
    }

    @Transactional
    public boolean deleteItem(ShoppingItemDTO shoppingItemDTO) {
        if(shoppingItemDTO.getId() == null){
            throw new InvalidShoppingItemRequest("ID field cannot be empty");
        }
        if (!itemRepo.existsById(shoppingItemDTO.getId())) {
            throw new ItemNotFound("Could not find item with ID: " + shoppingItemDTO.getId());
        }

        itemRepo.deleteById(shoppingItemDTO.getId());
        return true;
    }

    @Transactional
    public boolean deleteAllItems() {
        List<ShoppingItemEntity> itemEntities = getAllCurrentItems();
        itemRepo.deleteAll(itemEntities);
        return true;
    }

    @Transactional
    public List<ShoppingItemDTO> unmarkAll() {
        List<ShoppingItemEntity> entities = getAllCurrentItems();
        entities
                .forEach(item -> item.setInCart(false));
        itemRepo.saveAll(entities);

        return entities
                .stream()
                .map(ShoppingItemEntity::getDTO)
                .collect(Collectors.toList());
    }

    private List<ShoppingItemEntity> getAllCurrentItems() {
        return itemRepo.findByShoppingList(listService.getCurrentList());
    }
}
