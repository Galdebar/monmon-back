package lt.galdebar.monmonmvc.persistence.dao;

public interface IsShoppingItemDao {
    void insertItem(ShoppingItem shoppingItem);
    ShoppingItem findByItemName(String itemName);

}
