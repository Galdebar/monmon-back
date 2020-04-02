package lt.galdebar.monmon.categoriesparser.services;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDAO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.repositories.CategoriesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Log4j2
@Component
public class CategoriesParserMain {

    @Autowired
    private CategoriesRepo categoriesRepo;

    @Autowired
    private ExcelParser excelParser;

    @Autowired
    private CategoryDTOtoDAOConverter converter;


    public boolean isParserValid(){
        return excelParser.isParserValid();
    }
    public void pushCategoriesToDB() {
        List<CategoryDAO> categoryDAOList = convertToDAO(getCategories());
        try{
            categoriesRepo.saveAll(categoryDAOList);
        }catch (DataIntegrityViolationException e){
            log.error("Categories parser failed to push to DB. Cause: " + e.getMessage());
        }
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
