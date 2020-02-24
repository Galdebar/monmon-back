package lt.galdebar.monmonmvc.service;


import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.persistence.dao.Category;
import lt.galdebar.monmonmvc.persistence.repositories.MongoDBRepo;
import lt.galdebar.monmonmvc.persistence.dao.ShoppingItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    @Autowired
    private final MongoDBRepo mongoDBRepo;

    @Autowired
    private HibernateSearchService hibernateSearchService;

    public ShoppingItem addItem(ShoppingItem shoppingItem) {
        Category shoppingItemCategory = hibernateSearchService.findCategoryByKeyword(
                shoppingItem.itemName
        );
        shoppingItem.itemCategory = shoppingItemCategory.getCategoryName();
        ShoppingItem returnedItem = mongoDBRepo.insert(shoppingItem);
        return returnedItem;
    }

    public Optional<ShoppingItem> getItemById(String id) {
        return mongoDBRepo.findById(id);
    }

    public ShoppingItem getItemByName(String itemName) {
        return mongoDBRepo.findByItemName(itemName);
    }

    public List<ShoppingItem> getItemsByCategory(String requestedCategory) {
        return mongoDBRepo.findByItemCategory(requestedCategory);
    }

    public List<ShoppingItem> getAll() {
        return mongoDBRepo.findAll();
    }

    public List<ShoppingItem> getAllNOTInCart() {
        return mongoDBRepo.findByIsInCart(false);
    }

    public ShoppingItem updateItem(ShoppingItem shoppingItem) {

        return mongoDBRepo.save(shoppingItem);
    }

    public List<ShoppingItem> updateItems(ShoppingItem[] shoppingItems) {
        return mongoDBRepo.saveAll(Arrays.asList(shoppingItems));
    }

    public void deleteItem(ShoppingItem shoppingItem) {
        mongoDBRepo.delete(shoppingItem);
    }

    public void deleteItems(ShoppingItem[] shoppingItems) {
        mongoDBRepo.deleteAll(Arrays.asList(shoppingItems));
    }
}
