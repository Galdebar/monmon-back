package lt.galdebar.monmonmvc.dao;

import lt.galdebar.monmonmvc.model.shoppingitem.ShoppingItem;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface MongoDBRepo extends MongoRepository<ShoppingItem, String> {
    ShoppingItem findByItemName(String itemName);
    List<ShoppingItem> findByItemCategory(String itemCategory);
    List<ShoppingItem> findByIsInCart(Boolean isInCart);
}
