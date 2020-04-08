package lt.galdebar.monmon.categoriesparser.services;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryEntity;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.repositories.CategoriesRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Categories parser parent class.<br>
 *     Uses ExcelParser, CategoryDTOToEntityConverter to read the excel file, ready entities and push to DB.
 *     The parser is meant to be run once on the app initiation.
 */
@Log4j2
@Component
public class CategoriesParserMain {

    @Autowired
    private CategoriesRepo categoriesRepo;

    @Autowired
    private ExcelParser excelParser;

    @Autowired
    private CategoryDTOToEntityConverter converter;


    /**
     * Checks if the Autowired ExcelParser is valid.
     *
     * @return the boolean
     */
    public boolean isParserValid(){
        return excelParser.isParserValid();
    }

    /**
     * Push categories to db. Stops if matching categories are found.
     */
    public void pushCategoriesToDB() {
        List<CategoryEntity> categoryEntityList = convertToDAO(getCategories());
        try{
            categoriesRepo.saveAll(categoryEntityList);
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

    private List<CategoryEntity> convertToDAO(List<CategoryDTO> categoryDTOList) {
        return converter.convertDTOsToEntities(categoryDTOList);
    }
}
