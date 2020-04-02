package lt.galdebar.monmon.categoriesparser.services;

import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryEntity;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CategoryDTOtoDAOConverter {

    public List<CategoryEntity> convertDTOsToDAOs(List<CategoryDTO> categoryDTOList) {
        List<CategoryEntity> categoryEntityList = new ArrayList<>();
        for (CategoryDTO categoryDTO : categoryDTOList) {
            categoryEntityList.add(convertDTOtoDAO(categoryDTO));
        }
        return categoryEntityList;
    }

    private CategoryEntity convertDTOtoDAO(CategoryDTO categoryDTO) {
        Set<KeywordEntity> keywords = new HashSet<>();
        for (String keyword : categoryDTO.getKeywords()) {
            KeywordEntity keywordEntity = new KeywordEntity();
            keywordEntity.setKeyword(keyword);
            keywords.add(keywordEntity);
        }
        return new CategoryEntity(
                categoryDTO.getCategoryName(),
                keywords
        );
    }
}
