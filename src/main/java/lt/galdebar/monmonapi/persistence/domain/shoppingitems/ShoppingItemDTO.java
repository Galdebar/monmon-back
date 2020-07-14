package lt.galdebar.monmonapi.persistence.domain.shoppingitems;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
public class ShoppingItemDTO {
    private Long id;
    private String itemName;
    private String itemCategory;
    private Integer quantity;
    private String comment;
    private boolean isInCart = false;

    public ShoppingItemDTO(String itemName) {
        this.itemName = itemName;
        this.itemCategory = "";
        this.quantity=1;
        this.comment="";
    }
}
