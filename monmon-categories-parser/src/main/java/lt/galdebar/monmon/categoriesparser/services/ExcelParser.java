package lt.galdebar.monmon.categoriesparser.services;

import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.services.pojos.ParsedExcelRow;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.springframework.core.io.Resource;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Parses the supplied Excel file into a list of ParsedExcelRow objects.<br>
 *     Also adds "Uncategorized"
 *     Whilst parsing splits the "Food,Beverages {@literal &} Tobacco" category into three separate categories.
 *     Also expands Food Items with subcategories, because the aim of the app is everyday shopping.
 *     Sheet name "Food,Beverages {@literal &} Tobacco" category name and names of categories to split it into are provided as private static final String fields.
 *
 */
@Log4j2
public class ExcelParser {
    private static final String SHEET_NAME = "Sheet1";
    private static final String FOOD_CATEGORY_NAME = "Food Items";
    private static final String TOBACCO_SUBCATEGORY_NAME_IN_SHEETS = "Tobacco Products";
    private static final String BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS = "Beverages";
    private static final String FOOD_BEVERAGES_TOBACCO = "Food, Beverages & Tobacco";
    private static final String UNCATEGORIZED_TITLE = "Uncategorized";
    private Workbook workbook;
    private Sheet sheet;
    private DataFormatter dataFormatter;
    private boolean isParserValid;

    /**
     * Instantiates a new Excel parser.If file isn't found, sets isParserValid = false;
     *
     * @param filePath the file path String
     */
    public ExcelParser(String filePath) {
        try {
            workbook = WorkbookFactory.create(new File(filePath));
            sheet = workbook.getSheet(SHEET_NAME);
            dataFormatter = new DataFormatter();
            isParserValid = true;
            log.info("Excel Parser loaded.");
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            isParserValid = false;
        }
    }

    /**
     * Instantiates a new Excel parser.If file isn't found, sets isParserValid = false;
     *
     * @param file the file
     */
    public ExcelParser(Resource file) {
        try {
            workbook = WorkbookFactory.create(file.getInputStream());
            sheet = workbook.getSheet(SHEET_NAME);
            dataFormatter = new DataFormatter();
            isParserValid = true;
            log.info("Excel Parser loaded.");
        } catch (IOException | InvalidFormatException e) {
            e.printStackTrace();
            isParserValid = false;
        }
    }

    /**
     * Checks if the parser is valid. Method should be called before executing the parser, otherwise will throw IOException.
     *
     * @return the boolean
     */
    public boolean isParserValid() {
        return isParserValid;
    }

    /**
     * Main Excel Parser method. Calls all other methods to generate ParsedExcelRow list.
     *
     * @return the categories
     */
    public List<CategoryDTO> getCategories() {
        List<ParsedExcelRow> unfilteredList = getUnfilteredCategories();
        List<ParsedExcelRow> consolidatedList = consolidateSimilarCategories(unfilteredList);
        List<ParsedExcelRow> finalList = removeEmptyEntries(consolidatedList);
        List<CategoryDTO> dtoList = rowsToDTOs(finalList);
        return dtoList;
    }

    /**
     * First parsing step. Simply reads the sheet into an unfiltered, unparsed list of ParsedExcelRow objects.
     *
     * @return the unfiltered categories
     */
    List<ParsedExcelRow> getUnfilteredCategories() {
        List<ParsedExcelRow> parsedExcelRowList = new ArrayList<>();
        parsedExcelRowList.add(createUncategorized());
        for (Row row : sheet) {
            ParsedExcelRow generatedDTO = generateObjectFromRow(row);
            parsedExcelRowList.add(generatedDTO);
        }
        return parsedExcelRowList;
    }

    private ParsedExcelRow createUncategorized() {
        return new ParsedExcelRow(UNCATEGORIZED_TITLE, "", "", new HashSet<String>());
    }

    /**
     * Reads a single Row object and produces a ParsedExcelRow object from it. Ignores the first column, because I don't need the category ID in Google nomenclature.
     *
     * @param row the row
     * @return the category dto
     */
    ParsedExcelRow generateObjectFromRow(Row row) {
        int column = 0; // First cell needs to be ignored, because I don't need the shoppingItemCategory ID
        ParsedExcelRow parsedExcelRow = new ParsedExcelRow();
        Set<String> keywords = new HashSet<>();

        for (Cell cell : row) {
            column++;
            String cellValue = dataFormatter.formatCellValue(cell);
            if (column != 1 && !cellValue.equals("")) {
                getCategoryName(column, parsedExcelRow, cellValue);
                getSubcategoryName(column, parsedExcelRow, cellValue);
                getFoodCategoryName(column, parsedExcelRow, cellValue);
                addKeywordsIfValid(parsedExcelRow.getKeywords(), cellValue);
            }
        }

        return parsedExcelRow;
    }

    /**
     * Adds the cell value to a set of category keywords
     * if the cell value is NOT "Food Items", "Tobacco Products", "Beverages" or "Food, Beverages {@literal &} Tobacco".
     * This ensures that category names are not amongst the keywords.
     * The category names are set as private final static String fields for the class.
     *
     * @param keywords  the keywords
     * @param cellValue the cell value
     */
    void addKeywordsIfValid(Set<String> keywords, String cellValue) {
        if (!cellValue.equals(FOOD_CATEGORY_NAME)
                && !cellValue.equals(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS)
                && !cellValue.equals(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS)
                && !cellValue.equals(FOOD_BEVERAGES_TOBACCO)) {
            keywords.add(cellValue);
        }
    }

    /**
     * Sets food category name. Will onl execute if ParsedExcelRow subcategory is "Food Items" and the supplied cell count is 4.<br>
     *     Since I've decided to split food items, tobacco products and beverages into separate categories, I had to shift where the category name is found by one column.
     *
     * @param column   the column
     * @param parsedExcelRow the category dto
     * @param cellValue   the cell value
     */
    void getFoodCategoryName(int column, ParsedExcelRow parsedExcelRow, String cellValue) {
        if (column == 4 && parsedExcelRow.getSubcategory().equals(FOOD_CATEGORY_NAME)) {
            parsedExcelRow.setFoodCategoryName(cellValue);
        }
    }

    /**
     * Only sets "Food Items".<br>
     *     This is neccessary because I wanted to expand the Food Items category.
     *
     * @param cellCount   the cell count
     * @param parsedExcelRow the category dto
     * @param cellValue   the cell value
     */
    void getSubcategoryName(int cellCount, ParsedExcelRow parsedExcelRow, String cellValue) {
        if (cellCount == 3 && cellValue.equals(FOOD_CATEGORY_NAME)) {
            parsedExcelRow.setSubcategory(cellValue);
        }
    }

    /**
     * Gets category name.<br>
     *     If column is 2, sets whatever cell value provided.
     *     If column is 3, checks whether it's a tobacco or beverages category and sets appropriately.
     *
     * @param column   the column count
     * @param parsedExcelRow the category dto
     * @param cellValue   the cell value
     */
    void getCategoryName(int column, ParsedExcelRow parsedExcelRow, String cellValue) {
        if (column == 2) {
            parsedExcelRow.setCategoryName(cellValue);
        }

        if (column == 3 && cellValue.equals(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS)) {
            parsedExcelRow.setCategoryName(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS);
        } else if (column == 3 && cellValue.equals(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS)) {
            parsedExcelRow.setCategoryName(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS);
        }
    }

    /**
     * First filtering step.<br>
     *     If subcategory is "Food Items", then updates subcategory with whatever Food category was asigned.
     *     Checks unfiltered ParsedExcelRow objects in pairs.
     *     If object categories match, then keywords from object 2 are added to are added to object1, object1 is saved to filtered list whilst object 2 is discarded.
     *
     * @param unfilteredList the unfiltered list
     * @return filtered list
     */
    List<ParsedExcelRow> consolidateSimilarCategories(List<ParsedExcelRow> unfilteredList) {
        List<ParsedExcelRow> filteredList = new ArrayList<>();

        for (ParsedExcelRow unfilteredParsedExcelRow : unfilteredList) {
            if (filteredList.size() == 0) {
                filteredList.add(unfilteredParsedExcelRow);
                continue;
            }


            ParsedExcelRow lastObjectInFilteredList = filteredList.get(filteredList.size() - 1);
            if (unfilteredParsedExcelRow.getSubcategory().equals(FOOD_CATEGORY_NAME)) {
                unfilteredParsedExcelRow.setCategoryName(unfilteredParsedExcelRow.getFoodCategoryName());
            }
            if (lastObjectInFilteredList.getCategoryName().equals(unfilteredParsedExcelRow.getCategoryName())) {
                lastObjectInFilteredList.getKeywords().addAll(unfilteredParsedExcelRow.getKeywords());
            } else {
                filteredList.add(unfilteredParsedExcelRow);
            }

        }
        return filteredList;
    }

    /**
     * Additional list filtering step.<br>
     *     Disregards all ParsedExcelRow objects that
     *     <ul>
     *         <li>have empty category names</li>
     *         <li>are uncategorized and have more than 1 keyword</li>
     *         <li>category name is "Food, Beverages {@literal &} Tobacco"</li>
     *     </ul>
     *
     * @param parsedExcelRowList the category dto list
     * @return the filtered
     */
    List<ParsedExcelRow> removeEmptyEntries(List<ParsedExcelRow> parsedExcelRowList) {
        List<ParsedExcelRow> filteredList = parsedExcelRowList.stream()
                .filter(item -> !item.getCategoryName().equals(""))
                .distinct()
                .filter(item -> item.getKeywords().size() > 0 || item.getCategoryName().equals(UNCATEGORIZED_TITLE))
                .filter(item -> !item.getCategoryName().equals(FOOD_BEVERAGES_TOBACCO))
                .collect(Collectors.toList());
        return filteredList;
    }

    private List<CategoryDTO> rowsToDTOs(List<ParsedExcelRow> parsedExcelRows) {
        List<CategoryDTO> dtoList = new ArrayList<>();
        for(ParsedExcelRow rowDTO:parsedExcelRows){
            dtoList.add(
                    new CategoryDTO(
                            rowDTO.getCategoryName(),
                            rowDTO.getKeywords()
                    )
            );
        }

        return dtoList;
    }
}
