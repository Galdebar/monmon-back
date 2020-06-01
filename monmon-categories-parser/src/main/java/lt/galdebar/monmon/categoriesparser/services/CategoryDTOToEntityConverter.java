package lt.galdebar.monmon.categoriesparser.services;

import lt.galdebar.monmon.categoriesparser.persistence.domain.*;
import lt.galdebar.monmon.categoriesparser.services.pojos.ParsedExcelRow;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Helper class. Converts Category DTO objects into entities ready for storage in DB.
 */
@Component
public class CategoryDTOToEntityConverter {

    /**
     * Convert DTO list into Entity list.
     *
     * @param parsedExcelRowList the category dto list
     * @return the list
     */
    public List<ShoppingCategoryEntity> convertDTOsToEntities(List<ShoppingCategoryDTO> parsedExcelRowList) {
        List<ShoppingCategoryEntity> categoryEntityList = new ArrayList<>();
        for (ShoppingCategoryDTO categoryDTO : parsedExcelRowList) {
            categoryEntityList.add(convertDTOtoEntity(categoryDTO));
        }
        return categoryEntityList;
    }

    private ShoppingCategoryEntity convertDTOtoEntity(ShoppingCategoryDTO categoryDTO) {
        Set<ShoppingKeywordEntity> keywords = new HashSet<>();
        ShoppingCategoryEntity categoryEntity = new ShoppingCategoryEntity();
        for (String keyword : categoryDTO.getKeywords()) {
            ShoppingKeywordEntity keywordEntity = new ShoppingKeywordEntity();
            keywordEntity.setKeyword(keyword);
            keywordEntity.setShoppingItemCategory(categoryEntity);
            keywords.add(keywordEntity);
        }
        categoryEntity.setCategoryName(categoryDTO.getCategoryName());
        categoryEntity.setKeywords(keywords);
        return categoryEntity;
//        return new ShoppingCategoryEntity(
//                categoryDTO.getCategoryName(),
//                keywords
//        );
    }

    ShoppingCategoryDTO convertEntityToDTO(ShoppingCategoryEntity entity){
        Set<String> keywords = new HashSet<>();
        for(ShoppingKeywordEntity keywordEntity:entity.getKeywords()){
            keywords.add(keywordEntity.getKeyword());
        }
        return new ShoppingCategoryDTO(
                entity.getCategoryName(),
                keywords
        );
    }

    List<ShoppingCategoryDTO> convertEntitiesToDTOs(List<ShoppingCategoryEntity> categoryEntities) {
        List<ShoppingCategoryDTO> categoryDTOS = new ArrayList<>();
        for(ShoppingCategoryEntity entity:categoryEntities){
            categoryDTOS.add(convertEntityToDTO(entity));
        }

        return categoryDTOS;
    }
}
