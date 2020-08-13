package lt.galdebar.monmonapi.app.persistence.domain.shoppinglists;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Data
public class ShoppingListDTO {
    private Long id;
    private String name;
    private String password;
    private LocalDateTime timeCreated;
    private LocalDateTime lastUsedTime;
}
