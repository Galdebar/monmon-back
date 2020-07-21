package lt.galdebar.monmonapi.persistence.domain.shoppingitems;

import lombok.*;

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


}
