package lt.galdebar.monmonmvc.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

//@NoArgsConstructor
//@RequiredArgsConstructor
@Accessors(fluent = true)
@Document(collection = "items")
public class Item {
    @Id
    private String id;
    public String itemName;

    public Item(String itemName) {
        this.itemName = itemName;
    }

    public Item() {
    }

    @Override
    public String toString() {
        return "Item{" +
                "itemName='" + itemName + '\'' +
                '}';
    }
}
