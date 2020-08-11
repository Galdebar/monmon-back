package lt.galdebar.monmonapi.services.shoppingitems;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemEntity;
import lt.galdebar.monmonapi.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonapi.services.shoppingitems.exceptions.ItemNotFound;
import lt.galdebar.monmonapi.services.shoppinglists.ShoppingListService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingItemService {

    private final ShoppingItemRepo itemRepo;
    private final ShoppingListService listService;

    public List<ShoppingItemDTO> getAll() {
        List<ShoppingItemEntity> foundItems = itemRepo.findByShoppingList(
                listService.getCurrentList()
        );
        return foundItems.stream().map(ShoppingItemEntity::getDTO).collect(Collectors.toList());
    }

    public ShoppingItemDTO addItem(ShoppingItemDTO shoppingItemDTO) {
        //add category
        //find deal
        ShoppingItemEntity entityToSave = new ShoppingItemEntity(shoppingItemDTO);
        return itemRepo.save(entityToSave).getDTO();
    }

    public ShoppingItemDTO updateItem(ShoppingItemDTO shoppingItemDTO) {
        if (!itemRepo.existsById(shoppingItemDTO.getId())) {
            throw new ItemNotFound("Could not find item with ID: " + shoppingItemDTO.getId());
        }
        ShoppingItemEntity itemToSave = new ShoppingItemEntity(shoppingItemDTO);

        return itemRepo.save(itemToSave).getDTO();
    }

    public boolean deleteItem(ShoppingItemDTO shoppingItemDTO) {
        if (!itemRepo.existsById(shoppingItemDTO.getId())) {
            throw new ItemNotFound("Could not find item with ID: " + shoppingItemDTO.getId());
        }

        itemRepo.deleteById(shoppingItemDTO.getId());
        return true;
    }

}
