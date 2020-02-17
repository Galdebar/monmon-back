package lt.galdebar.monmonmvc.service;


import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.dao.InMemoryShoppingItemRepo;
import lt.galdebar.monmonmvc.dao.MongoDBRepo;
import lt.galdebar.monmonmvc.model.shoppingitem.ShoppingItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {
    @Autowired
    @Qualifier("inMemoryRepo")
    private final InMemoryShoppingItemRepo inMemoryItemRepo;

    @Autowired
    private final MongoDBRepo mongoDBRepo;

    public void addItem(ShoppingItem shoppingItem){
        inMemoryItemRepo.insertItem(shoppingItem);
        mongoDBRepo.insert(shoppingItem);
    }

    public Optional<ShoppingItem> getItemById(String id){
        return mongoDBRepo.findById(id);
    }

    public ShoppingItem getItemByName(String itemName){
        return mongoDBRepo.findByItemName(itemName);
    }

    public List<ShoppingItem> getItemsByCategory(String requestedCategory){
        return mongoDBRepo.findByItemCategory(requestedCategory);
    }

    public List<ShoppingItem> getAll() {
        return mongoDBRepo.findAll();
    }

    public List<ShoppingItem> getAllNOTInCart(){
        return mongoDBRepo.findByIsInCart(false);
    }

    public void updateItem(ShoppingItem shoppingItem){
        mongoDBRepo.save(shoppingItem);
    }

    public void updateItems(ShoppingItem[] shoppingItems){
        mongoDBRepo.saveAll(Arrays.asList(shoppingItems));
    }

    public void deleteItem(ShoppingItem shoppingItem){
        mongoDBRepo.delete(shoppingItem);
    }

    public void deleteItems(ShoppingItem[] shoppingItems){
        mongoDBRepo.deleteAll(Arrays.asList(shoppingItems));
    }
}
