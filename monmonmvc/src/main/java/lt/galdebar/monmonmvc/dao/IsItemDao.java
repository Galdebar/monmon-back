package lt.galdebar.monmonmvc.dao;

import lt.galdebar.monmonmvc.model.Item;

public interface IsItemDao {
    void insertItem(Item item);
    Item getItemByName(String itemName);
}
