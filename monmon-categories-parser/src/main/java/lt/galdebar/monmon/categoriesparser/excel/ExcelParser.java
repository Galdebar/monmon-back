package lt.galdebar.monmon.categoriesparser.excel;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ExcelParser {
    private static final File TAXONOMY_FILE = new File("monmon-categories-parser/src/main/resources/taxonomy-with-ids.en-US.xls");
    private static final String SHEET_NAME = "Sheet1";
    private static final String FOOD_CATEGORY_NAME = "Food Items";
    private static final String TOBACCO_SUBCATEGORY_NAME_IN_SHEETS = "Tobacco Products";
    private static final String BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS = "Beverages";
    private static final String FOOD_BEVERAGES_TOBACCO = "Food, Beverages & Tobacco";
    private static final String UNCATEGORIZED_TITLE = "Uncategorized";
    private static Workbook workbook;
    private static Sheet sheet;
    private static DataFormatter dataFormatter;
    private static boolean isParserValid;

    public ExcelParser() {
        try {
            workbook = WorkbookFactory.create(TAXONOMY_FILE);
            sheet = workbook.getSheet(SHEET_NAME);
            dataFormatter = new DataFormatter();
            isParserValid = true;
        } catch (IOException e) {
            e.printStackTrace();
            isParserValid = false;
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            isParserValid = false;
        }
    }

    public ExcelParser(String filePath) {
        try {
            workbook = WorkbookFactory.create(new File(filePath));
            sheet = workbook.getSheet(SHEET_NAME);
            dataFormatter = new DataFormatter();
            isParserValid = true;
        } catch (IOException e) {
            e.printStackTrace();
            isParserValid = false;
        } catch (InvalidFormatException e) {
            e.printStackTrace();
            isParserValid = false;
        }
    }

    public boolean isParserValid() {
        return isParserValid;
    }

    public List<CategoryDTO> getCategories() {
        List<CategoryDTO> unfilteredList = getUnfilteredCategories();
        List<CategoryDTO> consolidatedList = consolidateSimilarCategories(unfilteredList);
        List<CategoryDTO> finalList = removeEmptyEntries(consolidatedList);
        return finalList;
    }

    List<CategoryDTO> getUnfilteredCategories() {
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        categoryDTOList.add(createUncategorized());
        for (Row row : sheet) {
            CategoryDTO generatedDTO = generateDTOFromRow(row);
            categoryDTOList.add(generatedDTO);
        }
        return categoryDTOList;
    }

    private CategoryDTO createUncategorized() {
        return new CategoryDTO(UNCATEGORIZED_TITLE, "", "", new HashSet<String>());
    }

    CategoryDTO generateDTOFromRow(Row row) {
        int cellCount = 0; // First cell needs to be ignored, because I don't need the shoppingItemCategory ID
        CategoryDTO categoryDTO = new CategoryDTO();
        Set<String> keywords = new HashSet<>();

        for (Cell cell : row) {
            cellCount++;
            String cellValue = dataFormatter.formatCellValue(cell);
            if (cellCount != 1 && !cellValue.equals("")) {
                getCategoryName(cellCount, categoryDTO, cellValue);
                getSubcategoryName(cellCount, categoryDTO, cellValue);
                getFoodCategoryName(cellCount, categoryDTO, cellValue);
                addKeywordsIfValid(categoryDTO.getKeywords(), cellValue);
            }
        }

        return categoryDTO;
    }

    void addKeywordsIfValid(Set<String> keywords, String cellValue) {
        if (!cellValue.equals(FOOD_CATEGORY_NAME)
                && !cellValue.equals(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS)
                && !cellValue.equals(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS)
                && !cellValue.equals(FOOD_BEVERAGES_TOBACCO)) {
            keywords.add(cellValue);
        }
    }

    void getFoodCategoryName(int cellCount, CategoryDTO categoryDTO, String cellValue) {
        if (cellCount == 4 && categoryDTO.getSubcategory().equals(FOOD_CATEGORY_NAME)) {
            categoryDTO.setFoodCategoryName(cellValue);
        }
    }

    void getSubcategoryName(int cellCount, CategoryDTO categoryDTO, String cellValue) {
        if (cellCount == 3 && cellValue.equals(FOOD_CATEGORY_NAME)) {
            categoryDTO.setSubcategory(cellValue);
        }
    }

    void getCategoryName(int cellCount, CategoryDTO categoryDTO, String cellValue) {
        if (cellCount == 2) {
            categoryDTO.setCategoryName(cellValue);
        }

        if (cellCount == 3 && cellValue.equals(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS)) {
            categoryDTO.setCategoryName(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS);
        } else if (cellCount == 3 && cellValue.equals(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS)) {
            categoryDTO.setCategoryName(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS);
        }
    }

    List<CategoryDTO> consolidateSimilarCategories(List<CategoryDTO> unfilteredList) {
        List<CategoryDTO> filteredList = new ArrayList<>();

        for (CategoryDTO unfilteredCategoryDTO : unfilteredList) {
            if (filteredList.size() == 0) {
                filteredList.add(unfilteredCategoryDTO);
                continue;
            }


            CategoryDTO lastObjectInFilteredList = filteredList.get(filteredList.size() - 1);
            if (unfilteredCategoryDTO.getSubcategory().equals(FOOD_CATEGORY_NAME)) {
                unfilteredCategoryDTO.setCategoryName(unfilteredCategoryDTO.getFoodCategoryName());
            }
            if (lastObjectInFilteredList.getCategoryName().equals(unfilteredCategoryDTO.getCategoryName())) {
                lastObjectInFilteredList.getKeywords().addAll(unfilteredCategoryDTO.getKeywords());
            } else {
                filteredList.add(unfilteredCategoryDTO);
            }

        }
        return filteredList;
    }

    List<CategoryDTO> removeEmptyEntries(List<CategoryDTO> categoryDTOList) {
        List<CategoryDTO> filteredList = categoryDTOList.stream()
                .filter(item -> !item.getCategoryName().equals(""))
                .distinct()
                .filter(item -> item.getKeywords().size() >1 || item.getCategoryName().equals(UNCATEGORIZED_TITLE))
                .filter(item -> !item.getCategoryName().equals(FOOD_BEVERAGES_TOBACCO))
                .collect(Collectors.toList());
        return filteredList;
    }
}