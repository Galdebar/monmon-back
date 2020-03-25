package lt.galdebar.monmon.categoriesparser.services;

import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDAO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDAO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class CategoryDTOtoDAOConverter {

    public List<CategoryDAO> convertDTOsToDAOs(List<CategoryDTO> categoryDTOList) {
        List<CategoryDAO> categoryDAOList = new ArrayList<>();
        for (CategoryDTO categoryDTO : categoryDTOList) {
            categoryDAOList.add(convertDTOtoDAO(categoryDTO));
        }
        return categoryDAOList;
    }

    private CategoryDAO convertDTOtoDAO(CategoryDTO categoryDTO) {
        Set<KeywordDAO> keywords = new HashSet<>();
        for (String keyword : categoryDTO.getKeywords()) {
            KeywordDAO keywordDAO = new KeywordDAO();
            keywordDAO.setKeyword(keyword);
            keywords.add(keywordDAO);
        }
        return new CategoryDAO(
                categoryDTO.getCategoryName(),
                keywords
        );
    }
}
