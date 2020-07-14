package lt.galdebar.monmonapi.persistence.domain.shoppingitems;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@Entity(name = "shopping_item")
@Table(name = "SHOPPIING_ITEMS")
public class ShoppingItemEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String itemName;
    private String itemCategory;
    private Integer quantity;
    private String comment;
    private boolean isInCart = false;

}
