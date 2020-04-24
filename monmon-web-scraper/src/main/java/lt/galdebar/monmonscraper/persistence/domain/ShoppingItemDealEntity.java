package lt.galdebar.monmonscraper.persistence.domain;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Document(collection = "deals")
public class ShoppingItemDealEntity {
    @Id
    private String id;
    private String itemKeyword;
    private String itemBrand;
    private String shopTitle;
    private float price;

    public ShoppingItemDealEntity() {
        this.itemKeyword ="";
        this.itemBrand = "";
        this.shopTitle = "";
        this.price = 0.0f;
    }
}
