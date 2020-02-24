package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.dao.IsShoppingItemDao;
import lt.galdebar.monmonmvc.persistence.dao.ShoppingItem;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("inMemoryRepo")
public class InMemoryShoppingItemRepo implements IsShoppingItemDao {
    private static List<ShoppingItem> shoppingItems = new ArrayList<>();


    public void insertItem(ShoppingItem shoppingItem){
        shoppingItems.add(shoppingItem);
    }

    public ShoppingItem findByItemName(String itemName) {
        ShoppingItem foundShoppingItem = null;
        for(ShoppingItem shoppingItem : shoppingItems){
            if(shoppingItem.itemName.equalsIgnoreCase(itemName)){
                foundShoppingItem = shoppingItem;
            }
        }
        return foundShoppingItem;
    }
}
