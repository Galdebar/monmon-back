package lt.galdebar.monmonmvc.model.shoppingitem;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
@Document(collection = "items")
public class ShoppingItem {
    @Id
    public String id;
    public String itemName;
    public ShoppingItemCategory itemCategory;
    public Integer quantity;
    public String comment;
    public boolean isInCart = false;



    @Override
    public String toString() {
        return "ShoppingItem{" +
                "id='" + id + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemCategory=" + itemCategory +
                ", quantity=" + quantity +
                ", comment='" + comment + '\'' +
                ", isInCart=" + isInCart +
                '}';
    }
}
