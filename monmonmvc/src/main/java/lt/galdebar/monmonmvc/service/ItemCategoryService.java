package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.dao.ShoppingItemCategory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemCategoryService {
    public Map<String, String> getItemCategories(){
        List<String> strings = new ArrayList<>();
        Map<String, String> map= new HashMap<>();
        for (ShoppingItemCategory category:ShoppingItemCategory.values()){
            strings.add(category.getOnScreenName());
            map.put(category.name(), category.getOnScreenName());
        }
        return map;
    }
}
