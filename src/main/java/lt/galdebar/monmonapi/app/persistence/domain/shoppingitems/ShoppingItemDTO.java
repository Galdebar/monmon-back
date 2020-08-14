package lt.galdebar.monmonapi.app.persistence.domain.shoppingitems;

import lombok.*;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.InvalidShoppingItemRequest;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ShoppingItemDTO {
    private Long id;
    private String itemName;
    private String itemCategory;
    private Integer quantity;
    private String comment;
    private boolean isInCart = false;

    public boolean checkIfValid() {
        if (this.itemName == null || this.itemName.trim().isEmpty()) {
            throw new InvalidShoppingItemRequest("Item must have a name");
        }
        if (this.quantity != null && this.quantity < 1) {
            throw new InvalidShoppingItemRequest("Item quantity must be greater than 1");
        }

        return true;
    }


}
