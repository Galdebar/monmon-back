package lt.galdebar.monmonapi.webscraper.persistence.domain;

import lombok.*;

@AllArgsConstructor
@Getter
@ToString
@EqualsAndHashCode
public class ShoppingItemDealDTO {
    private String itemKeyword;
    private String itemBrand;
    private String shopTitle;
    private float price;

    public ShoppingItemDealDTO() {
        this.itemKeyword ="";
        this.itemBrand = "";
        this.shopTitle = "";
        this.price = 0.0f;
    }
}
