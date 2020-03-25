package lt.galdebar.monmon.categoriesparser.services;

import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDAO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.repositories.CategoriesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CategoriesParserMain {

    @Autowired
    private CategoriesRepo categoriesRepo;

    @Autowired
    private ExcelParser excelParser;

    @Autowired
    private CategoryDTOtoDAOConverter converter;


    public void pushCategoriesToDB() {
        List<CategoryDAO> categoryDAOList = convertToDAO(getCategories());
        categoriesRepo.saveAll(categoryDAOList);
    }

    private List<CategoryDTO> getCategories() {
        if (excelParser.isParserValid()) {
            return excelParser.getCategories();
        }

        return null;
    }

    private List<CategoryDAO> convertToDAO(List<CategoryDTO> categoryDTOList) {
        return converter.convertDTOsToDAOs(categoryDTOList);
    }
}
