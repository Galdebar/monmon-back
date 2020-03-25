package lt.galdebar.monmon.categoriesparser.excel;


import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDAO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.repositories.CategoriesRepo;
import lt.galdebar.monmon.categoriesparser.persistence.repositories.KeywordsRepo;
import lt.galdebar.monmon.categoriesparser.services.CategoriesParserMain;
import lt.galdebar.monmon.categoriesparser.services.CategoryDTOtoDAOConverter;
import lt.galdebar.monmon.categoriesparser.services.ExcelParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
public class ExcelParserIntegrationTests {
    @Autowired
    private CategoriesParserMain parserMain;

    @Autowired
    private KeywordsRepo keywordsRepo;

    @Autowired
    private CategoriesRepo categoriesRepo;

    @Autowired
    private ExcelParser excelParser;

    @Autowired
    private CategoryDTOtoDAOConverter converter;

    @Test
    public void givenContext_whenLoaded_thenParserValid(){
        assertNotNull(parserMain);
        assertNotNull(keywordsRepo);
        assertNotNull(categoriesRepo);
        assertNotNull(excelParser);
        assertTrue(excelParser.isParserValid());
    }

    @Test
    public void whenPushToDB_thenValidDBEntries(){
        List<CategoryDAO> expectedList = converter.convertDTOsToDAOs(excelParser.getCategories());
        parserMain.pushCategoriesToDB();

        List<CategoryDAO> actualList = categoriesRepo.findAll();
        expectedList.sort(this::compareCategoryDAO);
        actualList.sort(this::compareCategoryDAO);

        assertEquals(expectedList.size(), actualList.size());
        assertEquals(expectedList,actualList);
    }

    private int compareCategoryDAO(CategoryDAO categoryA, CategoryDAO categoryB){
        return categoryA.getCategoryName().compareTo(categoryB.getCategoryName());
    }
}
