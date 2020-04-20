package lt.galdebar.monmon.categoriesparser.services;

import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class KeywordDTOToEntityConverter {

    List<KeywordDTO> convertEntitiesToDTOs(List<KeywordEntity> entityList) {
        List<KeywordDTO> dtoList = new ArrayList<>();
        for (KeywordEntity entity : entityList) {
            dtoList.add(convertEntityToDTO(entity));
        }
        return dtoList;
    }

    private KeywordDTO convertEntityToDTO(KeywordEntity entity) {
        return new KeywordDTO(
                entity.getCategory().getCategoryName(),
                entity.getKeyword()
        );
    }
}
