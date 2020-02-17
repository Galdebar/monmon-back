package lt.galdebar.monmonmvc.dao;

import lt.galdebar.monmonmvc.model.shoppingitem.ShoppingItem;

public interface IsShoppingItemDao {
    void insertItem(ShoppingItem shoppingItem);
    ShoppingItem findByItemName(String itemName);

}
