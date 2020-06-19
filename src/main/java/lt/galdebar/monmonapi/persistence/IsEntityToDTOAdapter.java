package lt.galdebar.monmonapi.persistence;

import java.util.List;
import java.util.stream.Collectors;

public interface IsEntityToDTOAdapter<Entity, DTO> {
    Entity dtoToEntity(DTO dto);

    DTO entityToDTO(Entity entity);

    default List<Entity> dtoToEntity(List<DTO> dtoList) {
        return dtoList.stream().map(this::dtoToEntity).collect(Collectors.toList());
    }

    default List<DTO> entityToDTO(List<Entity> entityList) {
        return entityList.stream().map(this::entityToDTO).collect(Collectors.toList());
    }
}
