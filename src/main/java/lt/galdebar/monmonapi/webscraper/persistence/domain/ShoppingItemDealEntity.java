package lt.galdebar.monmonapi.webscraper.persistence.domain;

import lombok.*;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Data
@Entity
@Indexed
@Table(name = "deals")
public class ShoppingItemDealEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(columnDefinition="TEXT")
    @Field
    private String title;
    @Column(columnDefinition="TEXT")
    private String brand;
    private String shopTitle;
    private float price;

    public ShoppingItemDealEntity() {
        this.title ="";
        this.brand = "";
        this.shopTitle = "";
        this.price = 0.0f;
    }

    public ShoppingItemDealEntity(ShoppingItemDealDTO dto) {
        this.title = dto.getTitle();
        this.brand = dto.getBrand();
        this.shopTitle = dto.getShopTitle();
        this.price = dto.getPrice();
    }

    public ShoppingItemDealDTO getDTO(){
        return new ShoppingItemDealDTO(
                title,
                brand,
                shopTitle,
                price
        );
    }
}
