package lt.galdebar.monmonapi.webscraper.persistence.domain;

import lombok.*;

import javax.persistence.*;

@Data
//@Document(collection = "deals")
@Entity
@Table(name = "deals")
public class ShoppingItemDealEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(columnDefinition="TEXT")
    private String itemKeyword;
    @Column(columnDefinition="TEXT")
    private String itemBrand;
    private String shopTitle;
    private float price;

    public ShoppingItemDealEntity() {
        this.itemKeyword ="";
        this.itemBrand = "";
        this.shopTitle = "";
        this.price = 0.0f;
    }

    public ShoppingItemDealDTO getDTO(){
        return new ShoppingItemDealDTO(
                itemKeyword,
                itemBrand,
                shopTitle,
                price
        );
    }
}
