package lt.galdebar.monmonapi.webscraper.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

@AllArgsConstructor
@Data
@ToString
@EqualsAndHashCode
public class ShoppingItemDealDTO {

    @JsonIgnore
    private String untranslatedTitle;
    private String title;
    private String brand;
    private String shopTitle;
    private float price;

    public ShoppingItemDealDTO() {
        this.untranslatedTitle="";
        this.title ="";
        this.brand = "";
        this.shopTitle = "";
        this.price = 0.0f;
    }
}
