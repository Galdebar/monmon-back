package lt.galdebar.monmonmvc.persistence.domain.dao;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@NoArgsConstructor
@AllArgsConstructor
@Accessors(fluent = true)
@Document(collection = "items")
public class ShoppingItemDAO {
    @Id
    public String id;
    public String itemName;
    public String itemCategory;
    public Integer quantity;
    public String comment;
    public boolean isInCart = false;



    @Override
    public String toString() {
        return "ShoppingItemDAO{" +
                "id='" + id + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemCategory=" + itemCategory +
                ", quantity=" + quantity +
                ", comment='" + comment + '\'' +
                ", isInCart=" + isInCart +
                '}';
    }
}
