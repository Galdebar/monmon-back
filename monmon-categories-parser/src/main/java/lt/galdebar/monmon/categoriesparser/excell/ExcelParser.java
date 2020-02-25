package lt.galdebar.monmon.categoriesparser.excell;

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

    public boolean isParserValid() {
        return isParserValid;
    }

    public List<CategoryDTO> getCategories() {
        return trimList(getUnfilteredCategories());
    }

    private List<CategoryDTO> getUnfilteredCategories() {
        List<CategoryDTO> categoryDTOList = new ArrayList<>();
        categoryDTOList.add(createUncategorized());
        for (Row row : sheet) {
            generateDTOFromRow(categoryDTOList, row);
        }
        return categoryDTOList;
    }

    private CategoryDTO createUncategorized() {
        return new CategoryDTO("Uncategorized", "", "", new HashSet<String>());
    }

    private void generateDTOFromRow(List<CategoryDTO> categoryDTOList, Row row) {
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

//                categoryName = getCategoryName(cellCount, cellValue, categoryName);
//                subcategory = getSubcategoryName(cellCount, cellValue, subcategory);
//                foodCategoryName = getFoodCategoryName(cellCount, subcategory, cellValue, subcategory);
//                addKeywordIfValid(keywords, cellValue);
//
//                if (cellCount == 2) {
//                    categoryDTO.setCategoryName(cellValue);
//                }
//                if (!cellValue.equals("")) {
//                    getCategoryName(cellCount, categoryDTO, cellValue);
//
//                    getSubcategoryName(cellCount, categoryDTO, cellValue);
//
//                    getFoodCategoryName(cellCount, categoryDTO, cellValue);
//                    addKeywordsIfValid(categoryDTO.getKeywords(), cellValue);
//                }


            }
        }

        categoryDTOList.add(categoryDTO);
    }

    private void addKeywordsIfValid(Set<String> keywords, String cellValue) {
        if(!cellValue.equals(FOOD_CATEGORY_NAME)
        || !cellValue.equals(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS)
        || !cellValue.equals(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS)
        || !cellValue.equals(FOOD_BEVERAGES_TOBACCO)){
            keywords.add(cellValue);
        }
    }

    private void getFoodCategoryName(int cellCount, CategoryDTO categoryDTO, String cellValue) {
        if (cellCount == 4 && categoryDTO.getSubcategory().equals(FOOD_CATEGORY_NAME)) {
            categoryDTO.setFoodCategoryName(cellValue);
        }
    }

    private void getSubcategoryName(int cellCount, CategoryDTO categoryDTO, String cellValue) {
        if (cellCount == 3 && cellValue.equals(FOOD_CATEGORY_NAME)) {
            categoryDTO.setSubcategory(cellValue);
        }
    }

    private void getCategoryName(int cellCount, CategoryDTO categoryDTO, String cellValue) {
        if (cellCount == 2) {
            categoryDTO.setCategoryName(cellValue);
        }

        if (cellCount == 3 && cellValue.equals(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS)) {
            categoryDTO.setCategoryName(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS);
        } else if (cellCount == 3 && cellValue.equals(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS)) {
            categoryDTO.setCategoryName(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS);
        }
    }

    private List<CategoryDTO> trimList(List<CategoryDTO> unfilteredList) {
        List<CategoryDTO> filteredList = new ArrayList<>();

        for (CategoryDTO categoryDTO : unfilteredList) {
            if (filteredList.size() == 0) {
                filteredList.add(categoryDTO);
                continue;
            }


            CategoryDTO lastObjectInFilteredList = filteredList.get(filteredList.size() - 1);
            if (categoryDTO.getSubcategory().equals(FOOD_CATEGORY_NAME)) {
                categoryDTO.setCategoryName(categoryDTO.getFoodCategoryName());
            }
            if (lastObjectInFilteredList.getCategoryName().equals(categoryDTO.getCategoryName())) {
                lastObjectInFilteredList.getKeywords().addAll(categoryDTO.getKeywords());
            } else {
                filteredList.add(categoryDTO);
            }

        }

        filteredList = filteredList.stream()
                .filter(item -> !item.getCategoryName().equals(""))
                .distinct()
                .filter(item -> item.getKeywords().size() != 1)
                .filter(item -> !item.getCategoryName().equals(FOOD_BEVERAGES_TOBACCO))
                .collect(Collectors.toList());

        for(CategoryDTO categoryDTO:filteredList){
            System.out.println(categoryDTO);
        }
        return filteredList;
    }
}
