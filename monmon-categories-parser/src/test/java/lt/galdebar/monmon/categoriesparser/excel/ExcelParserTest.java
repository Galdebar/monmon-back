package lt.galdebar.monmon.categoriesparser.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ExcelParserTest {
    private static final String TAXONOMY_FILE_FULL = "src/test/testresources/Excel_test_full.xls";
    private static final String TAXONOMY_FILE_FEW_ROWS = "src/test/testresources/Excel_test_fewRows.xls";
    private static final String TAXONOMY_FILE_FEW_ROWS_FOOD = "src/test/testresources/Excel_test_fewRowsFood.xls";
    private static final String TAXONOMY_FILE_ONE_ROW = "src/test/testresources/Excel_test_oneRow.xls";
    private static final String TAXONOMY_FILE_ONE_ROW_FOOD = "src/test/testresources/Excel_test_oneRowFood.xls";
    private static final String SHEET_NAME = "Sheet1";


    @Test
    void isParserValidTrue() {
        ExcelParser correctParser = new ExcelParser(TAXONOMY_FILE_ONE_ROW);

        assertTrue(correctParser.isParserValid());
    }

    @Test
    void isParserValidFalse(){

        Exception exception = assertThrows(FileNotFoundException.class, ()->{
            new ExcelParser("/monmon/something").isParserValid();
        } );
    }


    @Test
    void getCategories() {
        ExcelParser parser = new ExcelParser(TAXONOMY_FILE_FEW_ROWS);
        List<CategoryDTO> expectedList = new ArrayList<>();
        List<CategoryDTO> actualList = new ArrayList<>();

        String cell2Keyword = "Arts & Entertainment";
        String cell3Keyword = "Hobbies & Creative Arts";
        String cell4Keyword = "Arts & Crafts";
        String cell5Keyword = "Art & Crafting Tools";
        String cell6Keyword = "Thread & Yarn Tools";

        String cell7Keyword1 = "Fiber Cards & Brushes";
        String cell7Keyword2 = "Hand Spindles";
        String cell7Keyword3 = "Needle Threaders";
        String cell7Keyword4 = "Thread & Yarn Guides";
        String cell7Keyword5 = "Thread & Yarn Spools";
        String cell7Keyword6 = "Thread, Yarn & Bobbin Winders";
        String cell7Keyword7 = "Weaving Beaters";
        String cell7Keyword8 = "Weaving Shuttles";

        Set<String> expectedCategoryKeywords = new HashSet<>();
        expectedCategoryKeywords.add(cell2Keyword);
        expectedCategoryKeywords.add(cell3Keyword);
        expectedCategoryKeywords.add(cell4Keyword);
        expectedCategoryKeywords.add(cell5Keyword);
        expectedCategoryKeywords.add(cell6Keyword);
        expectedCategoryKeywords.add(cell7Keyword1);
        expectedCategoryKeywords.add(cell7Keyword2);
        expectedCategoryKeywords.add(cell7Keyword3);
        expectedCategoryKeywords.add(cell7Keyword4);
        expectedCategoryKeywords.add(cell7Keyword5);
        expectedCategoryKeywords.add(cell7Keyword6);
        expectedCategoryKeywords.add(cell7Keyword7);
        expectedCategoryKeywords.add(cell7Keyword8);

        expectedList.add(createUncategorizedDTO());
        expectedList.add(
                new CategoryDTO(
                        cell2Keyword,
                        "",
                        "",
                        expectedCategoryKeywords
                )
        );

        actualList = parser.getCategories();

        assertEquals(expectedList, actualList);
    }


    @Test
    void getUnfilteredCategories() {
        ExcelParser parser = new ExcelParser(TAXONOMY_FILE_FEW_ROWS);
        List<CategoryDTO> expectedList = new ArrayList<>();
        List<CategoryDTO> actualList = new ArrayList<>();

        String cell2Keyword = "Arts & Entertainment";
        String cell3Keyword = "Hobbies & Creative Arts";
        String cell4Keyword = "Arts & Crafts";
        String cell5Keyword = "Art & Crafting Tools";
        String cell6Keyword = "Thread & Yarn Tools";

        String cell7Keyword1 = "Fiber Cards & Brushes";
        String cell7Keyword2 = "Hand Spindles";
        String cell7Keyword3 = "Needle Threaders";
        String cell7Keyword4 = "Thread & Yarn Guides";
        String cell7Keyword5 = "Thread & Yarn Spools";
        String cell7Keyword6 = "Thread, Yarn & Bobbin Winders";
        String cell7Keyword7 = "Weaving Beaters";
        String cell7Keyword8 = "Weaving Shuttles";

        Set<String> category1Keywords = new HashSet<>();
        category1Keywords.add(cell2Keyword);
        category1Keywords.add(cell3Keyword);
        category1Keywords.add(cell4Keyword);
        category1Keywords.add(cell5Keyword);
        category1Keywords.add(cell6Keyword);
        category1Keywords.add(cell7Keyword1);
        Set<String> category2Keywords = new HashSet<>();
        category2Keywords.add(cell2Keyword);
        category2Keywords.add(cell3Keyword);
        category2Keywords.add(cell4Keyword);
        category2Keywords.add(cell5Keyword);
        category2Keywords.add(cell6Keyword);
        category2Keywords.add(cell7Keyword2);
        Set<String> category3Keywords = new HashSet<>();
        category3Keywords.add(cell2Keyword);
        category3Keywords.add(cell3Keyword);
        category3Keywords.add(cell4Keyword);
        category3Keywords.add(cell5Keyword);
        category3Keywords.add(cell6Keyword);
        category3Keywords.add(cell7Keyword3);
        Set<String> category4Keywords = new HashSet<>();
        category4Keywords.add(cell2Keyword);
        category4Keywords.add(cell3Keyword);
        category4Keywords.add(cell4Keyword);
        category4Keywords.add(cell5Keyword);
        category4Keywords.add(cell6Keyword);
        category4Keywords.add(cell7Keyword4);
        Set<String> category5Keywords = new HashSet<>();
        category5Keywords.add(cell2Keyword);
        category5Keywords.add(cell3Keyword);
        category5Keywords.add(cell4Keyword);
        category5Keywords.add(cell5Keyword);
        category5Keywords.add(cell6Keyword);
        category5Keywords.add(cell7Keyword5);
        Set<String> category6Keywords = new HashSet<>();
        category6Keywords.add(cell2Keyword);
        category6Keywords.add(cell3Keyword);
        category6Keywords.add(cell4Keyword);
        category6Keywords.add(cell5Keyword);
        category6Keywords.add(cell6Keyword);
        category6Keywords.add(cell7Keyword6);
        Set<String> category7Keywords = new HashSet<>();
        category7Keywords.add(cell2Keyword);
        category7Keywords.add(cell3Keyword);
        category7Keywords.add(cell4Keyword);
        category7Keywords.add(cell5Keyword);
        category7Keywords.add(cell6Keyword);
        category7Keywords.add(cell7Keyword7);
        Set<String> category8Keywords = new HashSet<>();
        category8Keywords.add(cell2Keyword);
        category8Keywords.add(cell3Keyword);
        category8Keywords.add(cell4Keyword);
        category8Keywords.add(cell5Keyword);
        category8Keywords.add(cell6Keyword);
        category8Keywords.add(cell7Keyword8);

        CategoryDTO expectedCategory1 = new CategoryDTO(
                cell2Keyword,
                "",
                "",
                category1Keywords
        );
        CategoryDTO expectedCategory2 = new CategoryDTO(
                cell2Keyword,
                "",
                "",
                category2Keywords
        );
        CategoryDTO expectedCategory3 = new CategoryDTO(
                cell2Keyword,
                "",
                "",
                category3Keywords
        );
        CategoryDTO expectedCategory4 = new CategoryDTO(
                cell2Keyword,
                "",
                "",
                category4Keywords
        );
        CategoryDTO expectedCategory5 = new CategoryDTO(
                cell2Keyword,
                "",
                "",
                category5Keywords
        );
        CategoryDTO expectedCategory6 = new CategoryDTO(
                cell2Keyword,
                "",
                "",
                category6Keywords
        );
        CategoryDTO expectedCategory7 = new CategoryDTO(
                cell2Keyword,
                "",
                "",
                category7Keywords
        );
        CategoryDTO expectedCategory8 = new CategoryDTO(
                cell2Keyword,
                "",
                "",
                category8Keywords
        );

        expectedList.add(createUncategorizedDTO());
        expectedList.add(expectedCategory1);
        expectedList.add(expectedCategory2);
        expectedList.add(expectedCategory3);
        expectedList.add(expectedCategory4);
        expectedList.add(expectedCategory5);
        expectedList.add(expectedCategory6);
        expectedList.add(expectedCategory7);
        expectedList.add(expectedCategory8);

        actualList = parser.getUnfilteredCategories();

        assertEquals(expectedList, actualList);
    }

    @Test
    void generateDTOFromRowNotFood() {
        ExcelParser parser = createDummyParser();
        Sheet sheet = getSheet(TAXONOMY_FILE_ONE_ROW);
        Set<String> expectedKeywords = new HashSet<>();
        expectedKeywords.add("Arts & Entertainment");
        expectedKeywords.add("Hobbies & Creative Arts");
        expectedKeywords.add("Arts & Crafts");
        expectedKeywords.add("Art & Crafting Tools");
        expectedKeywords.add("Thread & Yarn Tools");
        expectedKeywords.add("Fiber Cards & Brushes");
        CategoryDTO expectedDTO = new CategoryDTO(
                "Arts & Entertainment",
                "",
                "",
                expectedKeywords
        );

        CategoryDTO actualDTO = parser.generateDTOFromRow(sheet.getRow(0));

        assertEquals(expectedDTO, actualDTO);
    }

    @Test
    void generateDTOFromRowFood() {
        ExcelParser parser = createDummyParser();
        Sheet sheet = getSheet(TAXONOMY_FILE_ONE_ROW_FOOD);
        Set<String> expectedKeywords = new HashSet<>();
        expectedKeywords.add("Condiments & Sauces");
        expectedKeywords.add("Dessert Toppings");
        expectedKeywords.add("Ice Cream Syrup");
        CategoryDTO expectedDTO = new CategoryDTO(
                "Food, Beverages & Tobacco",
                "Food Items",
                "Condiments & Sauces",
                expectedKeywords
        );

        CategoryDTO actualDTO = parser.generateDTOFromRow(sheet.getRow(0));

        assertEquals(expectedDTO, actualDTO);
    }

    @Test
    void addKeywordsIfValid() {
        ExcelParser parser = createDummyParser();
        String testAcceptedString = "Accepted Keyword";
        String[] testStrings = {
                testAcceptedString,
                "Food Items",
                "Tobacco Products",
                "Beverages",
                "Food, Beverages & Tobacco"
        };
        Set<String> expectedSet = new HashSet<>();
        expectedSet.add(testAcceptedString);
        Set<String> actualSet = new HashSet<>();

        for (String s : testStrings) {
            parser.addKeywordsIfValid(actualSet, s);
        }

        assertEquals(expectedSet, actualSet);
    }

    @Test
    void getFoodCategoryName() {
        ExcelParser parser = createDummyParser();
        String categoryName = "Food Items";
        String cellValue1 = "9i1h9iuh12e";
        String cellValue2 = "aoknkkdiajwn";
        String cellValue3 = "paoiwjdpoawjd";
        String cellValue4 = "oknolkamnw";
        CategoryDTO testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setSubcategory(categoryName);
        String expectedFoodCategory = categoryName;
        String actualCategory;

        parser.getFoodCategoryName(1, testCategoryDTO, cellValue1);
        parser.getFoodCategoryName(2, testCategoryDTO, cellValue2);
        parser.getFoodCategoryName(3, testCategoryDTO, cellValue3);
        parser.getFoodCategoryName(4, testCategoryDTO, categoryName);
        parser.getFoodCategoryName(5, testCategoryDTO, cellValue4);

        actualCategory = testCategoryDTO.getFoodCategoryName();

        assertEquals(expectedFoodCategory, actualCategory);
    }
    @Test
    void getFoodCategoryNameFalse() {
        ExcelParser parser = createDummyParser();
        String categoryName = "Not Food Item";
        String cellValue1 = "9i1h9iuh12e";
        String cellValue2 = "aoknkkdiajwn";
        String cellValue3 = "paoiwjdpoawjd";
        String cellValue4 = "oknolkamnw";
        CategoryDTO testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setSubcategory(categoryName);
        String expectedFoodCategory = "";
        String actualCategory;

        parser.getFoodCategoryName(1, testCategoryDTO, cellValue1);
        parser.getFoodCategoryName(2, testCategoryDTO, cellValue2);
        parser.getFoodCategoryName(3, testCategoryDTO, cellValue3);
        parser.getFoodCategoryName(4, testCategoryDTO, cellValue4);
        parser.getFoodCategoryName(5, testCategoryDTO, categoryName);

        actualCategory = testCategoryDTO.getFoodCategoryName();

        assertEquals(expectedFoodCategory, actualCategory);
    }

    @Test
    void getFoodCategoryNameTrue() {
        ExcelParser parser = createDummyParser();
        String categoryName = "Food Items";
        String foodCategoryName = "Tasty Food Item";
        String cellValue1 = "9i1h9iuh12e";
        String cellValue2 = "aoknkkdiajwn";
        String cellValue3 = "paoiwjdpoawjd";
        String cellValue4 = "oknolkamnw";

        CategoryDTO testCategoryDTO = new CategoryDTO();
        testCategoryDTO.setSubcategory(categoryName);
        String expectedCategory = foodCategoryName;
        String actualCategory;

        parser.getFoodCategoryName(1, testCategoryDTO, cellValue1);
        parser.getFoodCategoryName(2, testCategoryDTO, cellValue2);
        parser.getFoodCategoryName(3, testCategoryDTO, cellValue3);
        parser.getFoodCategoryName(4, testCategoryDTO, foodCategoryName);
        parser.getFoodCategoryName(5, testCategoryDTO, cellValue4);

        actualCategory = testCategoryDTO.getFoodCategoryName();

        assertEquals(expectedCategory, actualCategory);
    }

    @Test
    void getSubcategoryName1() {
        ExcelParser parser = createDummyParser();
        String defaultFoodCategoryName = "Food Items";
        String cellValue1 = "9i1h9iuh12e";
        String cellValue2 = "aoknkkdiajwn";
        String cellValue3 = "paoiwjdpoawjd";
        String cellValue4 = "oknolkamnw";
        String cellValue5 = "oiwd";
        String expectedSubcategoryName = "";
        String actualSubcategoryName;

        CategoryDTO testCategoryDTO = new CategoryDTO();


        parser.getSubcategoryName(1, testCategoryDTO, cellValue1);
        parser.getSubcategoryName(2, testCategoryDTO, cellValue2);
        parser.getSubcategoryName(3, testCategoryDTO, cellValue3);
        parser.getSubcategoryName(4, testCategoryDTO, cellValue4);
        parser.getSubcategoryName(5, testCategoryDTO, cellValue5);
        parser.getSubcategoryName(5, testCategoryDTO, defaultFoodCategoryName);

        actualSubcategoryName = testCategoryDTO.getSubcategory();

        assertEquals(expectedSubcategoryName, actualSubcategoryName);

    }

    @Test
    void getSubcategoryName2() {
        ExcelParser parser = createDummyParser();
        String defaultFoodCategoryName = "Food Items";
        String cellValue1 = "9i1h9iuh12e";
        String cellValue2 = "aoknkkdiajwn";
        String cellValue3 = "paoiwjdpoawjd";
        String cellValue4 = "oknolkamnw";
        String cellValue5 = "oiwd";
        String expectedSubcategoryName = defaultFoodCategoryName;
        String actualSubcategoryName;

        CategoryDTO testCategoryDTO = new CategoryDTO();


        parser.getSubcategoryName(1, testCategoryDTO, cellValue1);
        parser.getSubcategoryName(2, testCategoryDTO, cellValue2);
        parser.getSubcategoryName(3, testCategoryDTO, defaultFoodCategoryName);
        parser.getSubcategoryName(4, testCategoryDTO, cellValue3);
        parser.getSubcategoryName(5, testCategoryDTO, cellValue4);
        parser.getSubcategoryName(5, testCategoryDTO, cellValue5);

        actualSubcategoryName = testCategoryDTO.getSubcategory();

        assertEquals(expectedSubcategoryName, actualSubcategoryName);

    }

    @Test
    void getCategoryName() {
        ExcelParser parser = createDummyParser();
        CategoryDTO testCategoryDTO = new CategoryDTO();
        String expectedCategoryName;
        String actualCategoryName;

        String cellValue1 = "9i1h9iuh12e";
        String cellValue2 = "aoknkkdiajwn";
        String cellValue3 = "paoiwjdpoawjd";
        String cellValue4 = "oknolkamnw";
        String cellValue5 = "oiwd";

        parser.getCategoryName(1, testCategoryDTO, cellValue1);
        parser.getCategoryName(2, testCategoryDTO, cellValue2);
        parser.getCategoryName(3, testCategoryDTO, cellValue3);
        parser.getCategoryName(4, testCategoryDTO, cellValue4);
        parser.getCategoryName(5, testCategoryDTO, cellValue5);

        expectedCategoryName = cellValue2;
        actualCategoryName = testCategoryDTO.getCategoryName();

        assertEquals(expectedCategoryName, actualCategoryName);
    }

    @Test
    void consolidateSimilarCategoriesSimple() {
        ExcelParser parser = createDummyParser();
        CategoryDTO testCategoryDTO1 = new CategoryDTO();
        testCategoryDTO1.setCategoryName("TestCategory1");
        testCategoryDTO1.getKeywords().add("Keyword1");

        CategoryDTO testCategoryDTO2 = new CategoryDTO(
                testCategoryDTO1.getCategoryName(),
                testCategoryDTO1.getSubcategory(),
                testCategoryDTO1.getFoodCategoryName(),
                testCategoryDTO1.getKeywords()
        );
        testCategoryDTO2.getKeywords().add("Keyword2");

        List<CategoryDTO> testList = new ArrayList<>();
        testList.add(testCategoryDTO1);
        testList.add(testCategoryDTO2);

        List<CategoryDTO> expectedList = new ArrayList<>();
        List<CategoryDTO> actualList = new ArrayList<>();
        expectedList.add(testCategoryDTO2);

        actualList = parser.consolidateSimilarCategories(testList);

        assertEquals(expectedList, actualList);

    }

    @Test
    void consolidateSimilarCategories() {
        ExcelParser parser = new ExcelParser(TAXONOMY_FILE_FEW_ROWS);
        List<CategoryDTO> unfilteredList = new ArrayList<>();
        List<CategoryDTO> expectedList = new ArrayList<>();
        List<CategoryDTO> actualList = new ArrayList<>();
        String categoryName = "Arts & Entertainment";
        String subcategory = "";
        String foodCategoryName = "";
        Set<String> keywords = new HashSet<>();
        keywords.add("Arts & Entertainment");
        keywords.add("Hobbies & Creative Arts");
        keywords.add("Arts & Crafts");
        keywords.add("Art & Crafting Tools");
        keywords.add("Thread & Yarn Tools");

        keywords.add("Fiber Cards & Brushes");
        keywords.add("Hand Spindles");
        keywords.add("Needle Threaders");
        keywords.add("Thread & Yarn Guides");
        keywords.add("Thread & Yarn Spools");
        keywords.add("Thread, Yarn & Bobbin Winders");
        keywords.add("Weaving Beaters");
        keywords.add("Weaving Shuttles");


        expectedList.add(createUncategorizedDTO());
        expectedList.add(new CategoryDTO(
                categoryName,
                subcategory,
                foodCategoryName,
                keywords
        ));


        unfilteredList = parser.getUnfilteredCategories();
        actualList = parser.consolidateSimilarCategories(unfilteredList);

        assertEquals(expectedList, actualList);

    }

    @Test
    void consolidateSimilarCategoriesFood() {
        ExcelParser parser = new ExcelParser(TAXONOMY_FILE_FEW_ROWS_FOOD);
        List<CategoryDTO> unfilteredList = new ArrayList<>();
        List<CategoryDTO> expectedList = new ArrayList<>();
        List<CategoryDTO> actualList = new ArrayList<>();

        Set<String> foodCategoryKeywords = new HashSet<>();
        foodCategoryKeywords.add("Condiments & Sauces");
        foodCategoryKeywords.add("Cocktail Sauce");
        foodCategoryKeywords.add("Curry Sauce");
        foodCategoryKeywords.add("Dessert Toppings");
        CategoryDTO expectedFoodCategory = new CategoryDTO(
                "Condiments & Sauces",
                "Food Items",
                "Condiments & Sauces",
                foodCategoryKeywords
        );

        Set<String> tobaccoCategoryKeywords = new HashSet<>();
        tobaccoCategoryKeywords.add("Chewing Tobacco");
        tobaccoCategoryKeywords.add("Cigarettes");
        tobaccoCategoryKeywords.add("Cigars");
        CategoryDTO expectedTobaccoCategory1 = new CategoryDTO(
                "Tobacco Products",
                "",
                "",
                tobaccoCategoryKeywords
        );

        Set<String> beverageKeywords = new HashSet<>();
        beverageKeywords.add("Water");
        beverageKeywords.add("Carbonated Water");
        beverageKeywords.add("Flavored Carbonated Water");
        beverageKeywords.add("Unflavored Carbonated Water");
        beverageKeywords.add("Vinegar Drinks");
        CategoryDTO expectedBeveragesCategory = new CategoryDTO(
                "Beverages",
                "",
                "",
                beverageKeywords
        );

        expectedList.add(createUncategorizedDTO());
        expectedList.add(expectedFoodCategory);
        expectedList.add(expectedTobaccoCategory1);
        expectedList.add(expectedBeveragesCategory);

        unfilteredList = parser.getUnfilteredCategories();
        actualList = parser.consolidateSimilarCategories(unfilteredList);

        assertEquals(expectedList, actualList);


    }

    @Test
    void removeEmptyEntries() {
        ExcelParser parser = createDummyParser();
        List<CategoryDTO> fullList = new ArrayList<>();
        List<CategoryDTO> expectedList = new ArrayList<>();
        List<CategoryDTO> actualList = new ArrayList<>();

        Set<String> foodCategoryKeywords = new HashSet<>();
        foodCategoryKeywords.add("Condiments & Sauces");
        foodCategoryKeywords.add("Cocktail Sauce");
        foodCategoryKeywords.add("Curry Sauce");
        foodCategoryKeywords.add("Dessert Toppings");
        CategoryDTO expectedFoodCategory = new CategoryDTO(
                "Condiments & Sauces",
                "Food Items",
                "Condiments & Sauces",
                foodCategoryKeywords
        );

        Set<String> tobaccoCategoryKeywords = new HashSet<>();
        tobaccoCategoryKeywords.add("Chewing Tobacco");
        tobaccoCategoryKeywords.add("Cigarettes");
        tobaccoCategoryKeywords.add("Cigars");
        CategoryDTO expectedTobaccoCategory1 = new CategoryDTO(
                "Tobacco Products",
                "",
                "",
                tobaccoCategoryKeywords
        );

        CategoryDTO emptyCategory1 = new CategoryDTO(
                "",
                "",
                "",
                new HashSet<>()
        );
        CategoryDTO emptyCategory2 = new CategoryDTO(
                "Tobacco Products",
                "",
                "",
                new HashSet<>()
        );
        CategoryDTO emptyCategory3 = new CategoryDTO(
                "Food, Beverages & Tobacco",
                "",
                "anything",
                tobaccoCategoryKeywords
        );

        fullList.add(createUncategorizedDTO());
        fullList.add(expectedFoodCategory);
        fullList.add(expectedTobaccoCategory1);
        fullList.add(emptyCategory1);
        fullList.add(emptyCategory2);
        fullList.add(emptyCategory3);

        expectedList.add(createUncategorizedDTO());
        expectedList.add(expectedFoodCategory);
        expectedList.add(expectedTobaccoCategory1);

        actualList = parser.removeEmptyEntries(fullList);

        assertEquals(expectedList, actualList);
    }

    private ExcelParser createDummyParser() {
        return new ExcelParser(TAXONOMY_FILE_ONE_ROW_FOOD);
    }

    private CategoryDTO createUncategorizedDTO() {
        CategoryDTO categoryDTO = new CategoryDTO();
        categoryDTO.setCategoryName("Uncategorized");
        return categoryDTO;
    }

    private Sheet getSheet(String filePath) {
        try {
            Workbook workbook = WorkbookFactory.create(new File(filePath));
            return workbook.getSheet(SHEET_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidFormatException e) {
            e.printStackTrace();
        }
        return null;
    }
}