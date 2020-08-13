package lt.galdebar.monmonapi.categoriesparser.services;

import lt.galdebar.monmonapi.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingCategoryDTO;
import lt.galdebar.monmonapi.categoriesparser.services.pojos.ParsedExcelRow;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@TestPropertySource(locations = {"classpath:categoriesparser/test.properties"})
class ExcelParserTest {
    private static final String TAXONOMY_FILE_FULL = "src/test/resources/categoriesparser/Excel_test_full.xls";
    private static final String TAXONOMY_FILE_FEW_ROWS = "src/test/resources/categoriesparser/Excel_test_fewRows.xls";
    private static final String TAXONOMY_FILE_FEW_ROWS_FOOD = "src/test/resources/categoriesparser/Excel_test_fewRowsFood.xls";
    private static final String TAXONOMY_FILE_ONE_ROW = "src/test/resources/categoriesparser/Excel_test_oneRow.xls";
    private static final String TAXONOMY_FILE_ONE_ROW_FOOD = "src/test/resources/categoriesparser/Excel_test_oneRowFood.xls";
    private static final String SHEET_NAME = "Sheet1";


    @Test
    void isParserValidTrue() {
        ExcelParser correctParser = new ExcelParser(TAXONOMY_FILE_ONE_ROW);

        assertTrue(correctParser.isParserValid());
    }

//    @Test
//    void isParserValidFalse() {
//
//        assertThrows(FileNotFoundException.class, () -> {
//            new ExcelParser("/monmon/something");
//        });
//    }


    @Test
    void getCategories() {
        ExcelParser parser = new ExcelParser(TAXONOMY_FILE_FEW_ROWS);
        List<ShoppingCategoryDTO> expectedList = new ArrayList<>();
        List<ShoppingCategoryDTO> actualList;

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

        expectedList.add(new ShoppingCategoryDTO("Uncategorized", new HashSet<String>()));
        expectedList.add(
                new ShoppingCategoryDTO(
                        cell2Keyword,
                        expectedCategoryKeywords
                )
        );

        actualList = parser.getCategories();

        assertEquals(expectedList, actualList);
    }


    @Test
    void getUnfilteredCategories() {
        ExcelParser parser = new ExcelParser(TAXONOMY_FILE_FEW_ROWS);
        List<ParsedExcelRow> expectedList = new ArrayList<>();
        List<ParsedExcelRow> actualList;

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

        ParsedExcelRow expectedCategory1 = new ParsedExcelRow(
                cell2Keyword,
                "",
                "",
                category1Keywords
        );
        ParsedExcelRow expectedCategory2 = new ParsedExcelRow(
                cell2Keyword,
                "",
                "",
                category2Keywords
        );
        ParsedExcelRow expectedCategory3 = new ParsedExcelRow(
                cell2Keyword,
                "",
                "",
                category3Keywords
        );
        ParsedExcelRow expectedCategory4 = new ParsedExcelRow(
                cell2Keyword,
                "",
                "",
                category4Keywords
        );
        ParsedExcelRow expectedCategory5 = new ParsedExcelRow(
                cell2Keyword,
                "",
                "",
                category5Keywords
        );
        ParsedExcelRow expectedCategory6 = new ParsedExcelRow(
                cell2Keyword,
                "",
                "",
                category6Keywords
        );
        ParsedExcelRow expectedCategory7 = new ParsedExcelRow(
                cell2Keyword,
                "",
                "",
                category7Keywords
        );
        ParsedExcelRow expectedCategory8 = new ParsedExcelRow(
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
        ParsedExcelRow expectedDTO = new ParsedExcelRow(
                "Arts & Entertainment",
                "",
                "",
                expectedKeywords
        );

        ParsedExcelRow actualDTO = parser.generateObjectFromRow(sheet.getRow(0));

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
        ParsedExcelRow expectedDTO = new ParsedExcelRow(
                "Food, Beverages & Tobacco",
                "Food Items",
                "Condiments & Sauces",
                expectedKeywords
        );

        ParsedExcelRow actualDTO = parser.generateObjectFromRow(sheet.getRow(0));

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
        ParsedExcelRow testParsedExcelRow = new ParsedExcelRow();
        testParsedExcelRow.setSubcategory(categoryName);
        String expectedFoodCategory = categoryName;
        String actualCategory;

        parser.getFoodCategoryName(1, testParsedExcelRow, cellValue1);
        parser.getFoodCategoryName(2, testParsedExcelRow, cellValue2);
        parser.getFoodCategoryName(3, testParsedExcelRow, cellValue3);
        parser.getFoodCategoryName(4, testParsedExcelRow, categoryName);
        parser.getFoodCategoryName(5, testParsedExcelRow, cellValue4);

        actualCategory = testParsedExcelRow.getFoodCategoryName();

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
        ParsedExcelRow testParsedExcelRow = new ParsedExcelRow();
        testParsedExcelRow.setSubcategory(categoryName);
        String expectedFoodCategory = "";
        String actualCategory;

        parser.getFoodCategoryName(1, testParsedExcelRow, cellValue1);
        parser.getFoodCategoryName(2, testParsedExcelRow, cellValue2);
        parser.getFoodCategoryName(3, testParsedExcelRow, cellValue3);
        parser.getFoodCategoryName(4, testParsedExcelRow, cellValue4);
        parser.getFoodCategoryName(5, testParsedExcelRow, categoryName);

        actualCategory = testParsedExcelRow.getFoodCategoryName();

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

        ParsedExcelRow testParsedExcelRow = new ParsedExcelRow();
        testParsedExcelRow.setSubcategory(categoryName);
        String expectedCategory = foodCategoryName;
        String actualCategory;

        parser.getFoodCategoryName(1, testParsedExcelRow, cellValue1);
        parser.getFoodCategoryName(2, testParsedExcelRow, cellValue2);
        parser.getFoodCategoryName(3, testParsedExcelRow, cellValue3);
        parser.getFoodCategoryName(4, testParsedExcelRow, foodCategoryName);
        parser.getFoodCategoryName(5, testParsedExcelRow, cellValue4);

        actualCategory = testParsedExcelRow.getFoodCategoryName();

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

        ParsedExcelRow testParsedExcelRow = new ParsedExcelRow();


        parser.getSubcategoryName(1, testParsedExcelRow, cellValue1);
        parser.getSubcategoryName(2, testParsedExcelRow, cellValue2);
        parser.getSubcategoryName(3, testParsedExcelRow, cellValue3);
        parser.getSubcategoryName(4, testParsedExcelRow, cellValue4);
        parser.getSubcategoryName(5, testParsedExcelRow, cellValue5);
        parser.getSubcategoryName(5, testParsedExcelRow, defaultFoodCategoryName);

        actualSubcategoryName = testParsedExcelRow.getSubcategory();

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

        ParsedExcelRow testParsedExcelRow = new ParsedExcelRow();


        parser.getSubcategoryName(1, testParsedExcelRow, cellValue1);
        parser.getSubcategoryName(2, testParsedExcelRow, cellValue2);
        parser.getSubcategoryName(3, testParsedExcelRow, defaultFoodCategoryName);
        parser.getSubcategoryName(4, testParsedExcelRow, cellValue3);
        parser.getSubcategoryName(5, testParsedExcelRow, cellValue4);
        parser.getSubcategoryName(5, testParsedExcelRow, cellValue5);

        actualSubcategoryName = testParsedExcelRow.getSubcategory();

        assertEquals(expectedSubcategoryName, actualSubcategoryName);

    }

    @Test
    void getCategoryName() {
        ExcelParser parser = createDummyParser();
        ParsedExcelRow testParsedExcelRow = new ParsedExcelRow();
        String expectedCategoryName;
        String actualCategoryName;

        String cellValue1 = "9i1h9iuh12e";
        String cellValue2 = "aoknkkdiajwn";
        String cellValue3 = "paoiwjdpoawjd";
        String cellValue4 = "oknolkamnw";
        String cellValue5 = "oiwd";

        parser.getCategoryName(1, testParsedExcelRow, cellValue1);
        parser.getCategoryName(2, testParsedExcelRow, cellValue2);
        parser.getCategoryName(3, testParsedExcelRow, cellValue3);
        parser.getCategoryName(4, testParsedExcelRow, cellValue4);
        parser.getCategoryName(5, testParsedExcelRow, cellValue5);

        expectedCategoryName = cellValue2;
        actualCategoryName = testParsedExcelRow.getCategoryName();

        assertEquals(expectedCategoryName, actualCategoryName);
    }

    @Test
    void consolidateSimilarCategoriesSimple() {
        ExcelParser parser = createDummyParser();
        ParsedExcelRow testParsedExcelRow1 = new ParsedExcelRow();
        testParsedExcelRow1.setCategoryName("TestCategory1");
        testParsedExcelRow1.getKeywords().add("Keyword1");

        ParsedExcelRow testParsedExcelRow2 = new ParsedExcelRow(
                testParsedExcelRow1.getCategoryName(),
                testParsedExcelRow1.getSubcategory(),
                testParsedExcelRow1.getFoodCategoryName(),
                testParsedExcelRow1.getKeywords()
        );
        testParsedExcelRow2.getKeywords().add("Keyword2");

        List<ParsedExcelRow> testList = new ArrayList<>();
        testList.add(testParsedExcelRow1);
        testList.add(testParsedExcelRow2);

        List<ParsedExcelRow> expectedList = new ArrayList<>();
        List<ParsedExcelRow> actualList;
        expectedList.add(testParsedExcelRow2);

        actualList = parser.consolidateSimilarCategories(testList);

        assertEquals(expectedList, actualList);

    }

    @Test
    void consolidateSimilarCategories() {
        ExcelParser parser = new ExcelParser(TAXONOMY_FILE_FEW_ROWS);
        List<ParsedExcelRow> unfilteredList = new ArrayList<>();
        List<ParsedExcelRow> expectedList = new ArrayList<>();
        List<ParsedExcelRow> actualList = new ArrayList<>();
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
        expectedList.add(new ParsedExcelRow(
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
        List<ParsedExcelRow> unfilteredList = new ArrayList<>();
        List<ParsedExcelRow> expectedList = new ArrayList<>();
        List<ParsedExcelRow> actualList = new ArrayList<>();

        Set<String> foodCategoryKeywords = new HashSet<>();
        foodCategoryKeywords.add("Condiments & Sauces");
        foodCategoryKeywords.add("Cocktail Sauce");
        foodCategoryKeywords.add("Curry Sauce");
        foodCategoryKeywords.add("Dessert Toppings");
        ParsedExcelRow expectedFoodCategory = new ParsedExcelRow(
                "Condiments & Sauces",
                "Food Items",
                "Condiments & Sauces",
                foodCategoryKeywords
        );

        Set<String> tobaccoCategoryKeywords = new HashSet<>();
        tobaccoCategoryKeywords.add("Chewing Tobacco");
        tobaccoCategoryKeywords.add("Cigarettes");
        tobaccoCategoryKeywords.add("Cigars");
        ParsedExcelRow expectedTobaccoCategory1 = new ParsedExcelRow(
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
        ParsedExcelRow expectedBeveragesCategory = new ParsedExcelRow(
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
        List<ParsedExcelRow> fullList = new ArrayList<>();
        List<ParsedExcelRow> expectedList = new ArrayList<>();
        List<ParsedExcelRow> actualList = new ArrayList<>();

        Set<String> foodCategoryKeywords = new HashSet<>();
        foodCategoryKeywords.add("Condiments & Sauces");
        foodCategoryKeywords.add("Cocktail Sauce");
        foodCategoryKeywords.add("Curry Sauce");
        foodCategoryKeywords.add("Dessert Toppings");
        ParsedExcelRow expectedFoodCategory = new ParsedExcelRow(
                "Condiments & Sauces",
                "Food Items",
                "Condiments & Sauces",
                foodCategoryKeywords
        );

        Set<String> tobaccoCategoryKeywords = new HashSet<>();
        tobaccoCategoryKeywords.add("Chewing Tobacco");
        tobaccoCategoryKeywords.add("Cigarettes");
        tobaccoCategoryKeywords.add("Cigars");
        ParsedExcelRow expectedTobaccoCategory1 = new ParsedExcelRow(
                "Tobacco Products",
                "",
                "",
                tobaccoCategoryKeywords
        );

        ParsedExcelRow emptyCategory1 = new ParsedExcelRow(
                "",
                "",
                "",
                new HashSet<>()
        );
        ParsedExcelRow emptyCategory2 = new ParsedExcelRow(
                "Tobacco Products",
                "",
                "",
                new HashSet<>()
        );
        ParsedExcelRow emptyCategory3 = new ParsedExcelRow(
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

    private ParsedExcelRow createUncategorizedDTO() {
        ParsedExcelRow parsedExcelRow = new ParsedExcelRow();
        parsedExcelRow.setCategoryName("Uncategorized");
        return parsedExcelRow;
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