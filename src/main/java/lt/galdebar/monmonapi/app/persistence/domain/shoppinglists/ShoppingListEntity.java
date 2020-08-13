package lt.galdebar.monmonapi.app.persistence.domain.shoppinglists;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.InvalidListRequest;

import javax.persistence.*;
import java.time.LocalDateTime;

import static java.time.LocalDateTime.*;

@Data
@RequiredArgsConstructor
@Entity(name = "shopping_list")
@Table(name = "shopping_lists")
public class ShoppingListEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String name;
    private String password;
    private LocalDateTime timeCreated = now();
    private LocalDateTime lastUsedTime = now();

    public ShoppingListEntity(ShoppingListDTO dto) {
        checkIfDTOValid(dto);
        this.name = dto.getName();
        this.password = dto.getPassword();

        if(dto.getId()!= null){
            this.id = dto.getId();
        }

        if(dto.getTimeCreated()!=null){
            this.timeCreated = dto.getTimeCreated();
        }

        if(dto.getLastUsedTime()!=null){
            this.lastUsedTime = dto.getLastUsedTime();
        }

    }

    public ShoppingListDTO getDTO(){
        ShoppingListDTO dto = new ShoppingListDTO();
        dto.setId(id);
        dto.setName(name);
        dto.setPassword(password);
        dto.setTimeCreated(timeCreated);
        dto.setLastUsedTime(lastUsedTime);

        return dto;
    }

    private void checkIfDTOValid(ShoppingListDTO dto) {
        if (
                (dto.getName() == null && dto.getPassword() == null) ||
                        (dto.getName().trim().isEmpty() && dto.getName().trim().isEmpty())
        ) {
            throw new InvalidListRequest("List name and password fiends cannot be empty.");
        }

        if (dto.getName() == null || dto.getName().trim().isEmpty()) {
            throw new InvalidListRequest("List name cannot be empty");
        }

        if(dto.getPassword()==null || dto.getPassword().trim().isEmpty()){
            throw new InvalidListRequest("Password cannot be empty");
        }
    }
}
