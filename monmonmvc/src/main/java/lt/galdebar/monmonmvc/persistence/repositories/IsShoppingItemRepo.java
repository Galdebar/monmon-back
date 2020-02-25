package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingItemDAO;

public interface IsShoppingItemRepo {
    void insertItem(ShoppingItemDAO shoppingItemDAO);
    ShoppingItemDAO findByItemName(String itemName);

}
