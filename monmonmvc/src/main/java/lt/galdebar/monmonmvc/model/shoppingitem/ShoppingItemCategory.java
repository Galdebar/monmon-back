package lt.galdebar.monmonmvc.model.shoppingitem;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ShoppingItemCategory {
    UNCATEGORIZED("Uncategorized"),
    APPAREL_MEN("Apparel - Men"),
    APPAREL_WOMEN("Apparel - Women"),
    BAKERY_BREAD("Bakery & Bread"),
    BAKING_COOKING("Baking & Cooking"),
    BATH_BODY("Bath & Body"),
    BEANS_GRAINS("Beans & Grains"),
    BEVERAGES("Beverages"),
    BREAKVAST_CEREAL("Breakfast & Cereal"),
    CANNED("Canned"),
    CONDIMENTS("Condiments"),
    DAIRY("Dairy"),
    DELI("Deli"),
    ELECTRONICS("Electronics"),
    FROZEN("Frozen Foods"),
    HAIR("Hair"),
    HERBS_SPICES("Herbs & Spices"),
    HOUSEHOLD("Household"),
    KITCHEN_DINING("Kitchen & Dining"),
    MEAL_SOLUTIONS("Meal Solutions"),
    MEAT("Meat"),
    MEDICAL_PRODUCTS("Medical Products"),
    OFFICE_STATIONERY("Office - Stationery"),
    PASTA_NOODLES("Pasta & Noodles"),
    PRODUCE("Produce"),
    SEAFOOD("Seafood"),
    SNACKS("Snacks"),
    TOOLS("Tools"),
    TOOLS_HOME_IMPROVEMENT("Tools & Home Improvement"),
    TOYS_NOVELTY("Toys & Novelty"),
    VITAMINS_SUPPLEMENTS("Vitamins & Supplements"),
    ALCOHOL("Alcohol");

    @Getter
    @JsonValue
    private String onScreenName;

    ShoppingItemCategory(String onScreenName) {
        this.onScreenName = onScreenName;
    }

}
