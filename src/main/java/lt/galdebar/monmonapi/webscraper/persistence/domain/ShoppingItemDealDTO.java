package lt.galdebar.monmonapi.webscraper.persistence.domain;

import lombok.*;

@AllArgsConstructor
@Data
@ToString
@EqualsAndHashCode
public class ShoppingItemDealDTO {
    private String title;
    private String itemBrand;
    private String shopTitle;
    private float price;

    public ShoppingItemDealDTO() {
        this.title ="";
        this.itemBrand = "";
        this.shopTitle = "";
        this.price = 0.0f;
    }
}
