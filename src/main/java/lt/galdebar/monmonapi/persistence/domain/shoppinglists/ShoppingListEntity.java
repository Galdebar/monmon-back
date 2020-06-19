package lt.galdebar.monmonapi.persistence.domain.shoppinglists;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
@Entity(name = "shopping_list")
@Table(name = "SHOPPING_LISTS")
public class ShoppingListEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String password;
    private LocalDateTime timeCreated;
    private LocalDateTime lastUsedTime;
}
