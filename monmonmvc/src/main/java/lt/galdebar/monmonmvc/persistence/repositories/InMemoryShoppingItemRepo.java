package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingItemDAO;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("inMemoryRepo")
public class InMemoryShoppingItemRepo implements IsShoppingItemRepo {
    private static List<ShoppingItemDAO> shoppingItemDAOS = new ArrayList<>();


    public void insertItem(ShoppingItemDAO shoppingItemDAO){
        shoppingItemDAOS.add(shoppingItemDAO);
    }

    public ShoppingItemDAO findByItemName(String itemName) {
        ShoppingItemDAO foundShoppingItemDAO = null;
        for(ShoppingItemDAO shoppingItemDAO : shoppingItemDAOS){
            if(shoppingItemDAO.itemName.equalsIgnoreCase(itemName)){
                foundShoppingItemDAO = shoppingItemDAO;
            }
        }
        return foundShoppingItemDAO;
    }
}
