package lt.galdebar.monmonmvc.dao;

import lt.galdebar.monmonmvc.model.Item;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository("inMemoryRepo")
public class InMemoryItemRepo implements IsItemDao{
    private static List<Item> items = new ArrayList<>();


    public void insertItem(Item item){
        items.add(item);
    }

    public Item getItemByName(String itemName) {
        Item foundItem = null;
        for(Item item: items){
            if(item.itemName.equalsIgnoreCase(itemName)){
                foundItem = item;
            }
        }
        return foundItem;
    }
}
