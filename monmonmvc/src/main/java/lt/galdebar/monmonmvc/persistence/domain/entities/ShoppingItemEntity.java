package lt.galdebar.monmonmvc.persistence.domain.entities;

import lombok.*;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.Set;


@Accessors(fluent = true)
@Document(collection = "items")
@Data
public class ShoppingItemEntity {
    @Id
    public String id;
    public String itemName;
    public String itemCategory;
    public Integer quantity;
    public String comment;
    public boolean isInCart = false;
    @ToString.Exclude
    public Set<String> users = new HashSet<>();

}
