package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingItemEntity;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("inMemoryRepo")
public class InMemoryShoppingItemRepo implements IsShoppingItemRepo {
    private static List<ShoppingItemEntity> shoppingItemEntities = new ArrayList<>();


    public void insertItem(ShoppingItemEntity shoppingItemEntity){
        shoppingItemEntities.add(shoppingItemEntity);
    }

    public ShoppingItemEntity findByItemName(String itemName) {
        ShoppingItemEntity foundShoppingItemEntity = null;
        for(ShoppingItemEntity shoppingItemEntity : shoppingItemEntities){
            if(shoppingItemEntity.itemName.equalsIgnoreCase(itemName)){
                foundShoppingItemEntity = shoppingItemEntity;
            }
        }
        return foundShoppingItemEntity;
    }
}
