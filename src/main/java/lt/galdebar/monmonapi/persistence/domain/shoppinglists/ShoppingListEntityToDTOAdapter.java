package lt.galdebar.monmonapi.persistence.domain.shoppinglists;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.persistence.IsEntityToDTOAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShoppingListEntityToDTOAdapter implements IsEntityToDTOAdapter<ShoppingListEntity, ShoppingListDTO> {

    private final PasswordEncoder passwordEncoder;

    @Override
    public ShoppingListEntity dtoToEntity(ShoppingListDTO shoppingListDTO) {
        ShoppingListEntity entity = new ShoppingListEntity();
        entity.setName(shoppingListDTO.getName());
        entity.setPassword(shoppingListDTO.getPassword());
        entity.setTimeCreated(shoppingListDTO.getTimeCreated());
        entity.setLastUsedTime(shoppingListDTO.getLastUsedTime());
        if (shoppingListDTO.getId() != null) {
            entity.setId(shoppingListDTO.getId());
        }

        return entity;

    }

    @Override
    public ShoppingListDTO entityToDTO(ShoppingListEntity shoppingListEntity) {
        ShoppingListDTO dto = new ShoppingListDTO();
        dto.setId(shoppingListEntity.getId());
        dto.setName(shoppingListEntity.getName());
        dto.setPassword(shoppingListEntity.getPassword());
        dto.setTimeCreated(shoppingListEntity.getTimeCreated());
        dto.setLastUsedTime(shoppingListEntity.getLastUsedTime());
        return dto;
    }
}
