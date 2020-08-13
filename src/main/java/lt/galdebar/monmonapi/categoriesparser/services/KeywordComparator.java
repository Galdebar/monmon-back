package lt.galdebar.monmonapi.categoriesparser.services;

import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingKeywordDTO;

import java.util.Comparator;
import java.util.Map;

public class KeywordComparator implements Comparator<ShoppingKeywordDTO> {
    private static final Map<String, Integer> categoryPriorities = Map.ofEntries(
            Map.entry("Candied & Chocolate Covered Fruit", 3),
            Map.entry("Candy & Chocolate", 3),
            Map.entry("Bakery", 3),
            Map.entry("Condiments & Sauces", 3),
            Map.entry("Cooking & Baking Ingredients", 3),
            Map.entry("Dips & Spreads", 3),
            Map.entry("Food Gift Baskets", 3),
            Map.entry("Frozen Desserts & Novelties", 3),
            Map.entry("Snack Foods", 3),
            Map.entry("Soups & Broths", 2),
            Map.entry("Seasonings & Spices", 2),
            Map.entry("Nuts & Seeds", 2),
            Map.entry("Prepared Foods", 2),
            Map.entry("Pasta & Noodles", 2),
            Map.entry("Meat, Seafood & Eggs", 1),
            Map.entry("Tofu, Soy & Vegetarian Products", 1),
            Map.entry("Grains, Rice & Cereal", 1),
            Map.entry("Fruits & Vegetables", 1),
            Map.entry("Dairy Products", 1),
            Map.entry("Beverages",1)
    );

    public KeywordComparator() {
    }

    @Override
    public int compare(ShoppingKeywordDTO o1, ShoppingKeywordDTO o2) {

        if (isPriority(o1) && isPriority(o2)) {
            return comparePriorityEntities(o1, o2);
        }
        return 0;
    }

    private boolean isPriority(ShoppingKeywordDTO entity) {
        return categoryPriorities.containsKey(entity.getShoppingItemCategory());
    }

    private int comparePriorityEntities(ShoppingKeywordDTO o1, ShoppingKeywordDTO o2) {
        int o1Priority = getObjectPriority(o1);
        int o2Priority = getObjectPriority(o2);
        return o1Priority - o2Priority;
    }

    private int getObjectPriority(ShoppingKeywordDTO o1) {
        return categoryPriorities.get(o1.getShoppingItemCategory());
    }
}
