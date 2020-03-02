package lt.galdebar.monmon.categoriesparser.domain;

import lt.galdebar.monmon.categoriesparser.excel.CategoryDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CategoryDTOtoDAOService {

    public List<CategoryDAO> convertList(List<CategoryDTO> categoryDTOList) {
        List<CategoryDAO> categoryDAOList = new ArrayList<>();
        for (CategoryDTO categoryDTO : categoryDTOList) {
            categoryDAOList.add(convertSingle(categoryDTO));
        }
        return categoryDAOList;
    }

    private CategoryDAO convertSingle(CategoryDTO categoryDTO) {
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
