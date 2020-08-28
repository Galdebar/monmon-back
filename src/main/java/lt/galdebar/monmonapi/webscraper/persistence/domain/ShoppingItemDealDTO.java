package lt.galdebar.monmonapi.webscraper.persistence.domain;

import lombok.*;

@AllArgsConstructor
@Data
@ToString
@EqualsAndHashCode
public class ShoppingItemDealDTO {
    private String originalTitle;
    private String itemKeyword;
    private String itemBrand;
    private String shopTitle;
    private float price;

    public ShoppingItemDealDTO() {
        this.originalTitle="";
        this.itemKeyword ="";
        this.itemBrand = "";
        this.shopTitle = "";
        this.price = 0.0f;
    }
}
