package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingItemEntity;

public interface IsShoppingItemRepo {
    void insertItem(ShoppingItemEntity shoppingItemEntity);
    ShoppingItemEntity findByItemName(String itemName);

}
