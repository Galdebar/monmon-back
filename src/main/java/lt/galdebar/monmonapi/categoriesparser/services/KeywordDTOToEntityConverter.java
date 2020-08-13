package lt.galdebar.monmonapi.categoriesparser.services;

import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingKeywordDTO;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingKeywordEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
class KeywordDTOToEntityConverter {

    List<ShoppingKeywordDTO> convertEntitiesToDTOs(List<ShoppingKeywordEntity> entityList) {
        List<ShoppingKeywordDTO> dtoList = new ArrayList<>();
        for (ShoppingKeywordEntity entity : entityList) {
            dtoList.add(convertEntityToDTO(entity));
        }
        return dtoList;
    }

    private ShoppingKeywordDTO convertEntityToDTO(ShoppingKeywordEntity entity) {
        return new ShoppingKeywordDTO(
                entity.getShoppingItemCategory().getCategoryName(),
                entity.getKeyword()
        );
    }
}
