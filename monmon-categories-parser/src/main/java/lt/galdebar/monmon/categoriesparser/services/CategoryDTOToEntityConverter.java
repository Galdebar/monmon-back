package lt.galdebar.monmon.categoriesparser.services;

import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryEntity;
import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDTO;
import lt.galdebar.monmon.categoriesparser.services.pojos.ParsedExcelRow;
import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordEntity;
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
    public List<CategoryEntity> convertDTOsToEntities(List<CategoryDTO> parsedExcelRowList) {
        List<CategoryEntity> categoryEntityList = new ArrayList<>();
        for (CategoryDTO categoryDTO : parsedExcelRowList) {
            categoryEntityList.add(convertDTOtoEntity(categoryDTO));
        }
        return categoryEntityList;
    }

    private CategoryEntity convertDTOtoEntity(CategoryDTO parsedExcelRow) {
        Set<KeywordEntity> keywords = new HashSet<>();
        for (String keyword : parsedExcelRow.getKeywords()) {
            KeywordEntity keywordEntity = new KeywordEntity();
            keywordEntity.setKeyword(keyword);
            keywords.add(keywordEntity);
        }
        return new CategoryEntity(
                parsedExcelRow.getCategoryName(),
                keywords
        );
    }

    CategoryDTO convertEntityToDTO(CategoryEntity entity){
        Set<String> keywords = new HashSet<>();
        for(KeywordEntity keywordEntity:entity.getKeywords()){
            keywords.add(keywordEntity.getKeyword());
        }
        return new CategoryDTO(
                entity.getCategoryName(),
                keywords
        );
    }

    List<CategoryDTO> convertEntitiesToDTOs(List<CategoryEntity> categoryEntities) {
        List<CategoryDTO> categoryDTOS = new ArrayList<>();
        for(CategoryEntity entity:categoryEntities){
            categoryDTOS.add(convertEntityToDTO(entity));
        }

        return categoryDTOS;
    }
}
