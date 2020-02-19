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

public class ExcellParser {
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

    public ExcellParser() {
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
        int cellCount = 0; // First cell needs to be ignored, because I don't need the category ID
        CategoryDTO categoryDTO;
        String categoryName = "";
        String subcategory = "";
        String foodCategoryName = "";
        Set<String> keywords = new HashSet<>();

        for (Cell cell : row) {
            cellCount++;
            String cellValue = dataFormatter.formatCellValue(cell);
            if (cellCount != 1) {
//                categoryName = getCategoryName(cellCount, cellValue, categoryName);
//                subcategory = getSubcategoryName(cellCount, cellValue, subcategory);
//                foodCategoryName = getFoodCategoryName(cellCount, subcategory, cellValue, subcategory);
//                addKeywordIfValid(keywords, cellValue);

                if (cellCount == 2) {
                    categoryName = cellValue;
                }
                if (!cellValue.equals("")) {
                    if (cellCount == 3 && cellValue.equals(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS)) {
                        categoryName = TOBACCO_SUBCATEGORY_NAME_IN_SHEETS;
                    } else if (cellCount == 3 && cellValue.equals(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS)) {
                        categoryName = BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS;
                    }

                    if (cellCount == 3 && cellValue.equals(FOOD_CATEGORY_NAME)) {
                        subcategory = cellValue;
                    }

                    if (cellCount == 4 && subcategory.equals(FOOD_CATEGORY_NAME)) {
                        foodCategoryName = cellValue;
                    }
                    keywords.add(cellValue);
                }


            }
        }

        keywords.remove(FOOD_CATEGORY_NAME);
        keywords.remove(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS);
        keywords.remove(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS);
        keywords.remove(FOOD_BEVERAGES_TOBACCO);

        categoryDTO = new CategoryDTO(categoryName, subcategory, foodCategoryName, keywords);
//        System.out.println(categoryDTO);
        categoryDTOList.add(categoryDTO);
    }

    private void addKeywordIfValid(Set<String> keywords, String cellValue) {
        if (!cellValue.equals("")) {
            keywords.add(cellValue);
        }
    }


    private String getFoodCategoryName(int cellCount, String subcategoryName, String cellValue, String currentValue) {
        if (!cellValue.equals("")) {

            if (cellCount == 4 && subcategoryName.equals(FOOD_CATEGORY_NAME)) {
                return cellValue;
            }
        }
        return currentValue;
    }

    private String getSubcategoryName(int cellCount, String cellValue, String currentValue) {
        if (!cellValue.equals("")) {
            if (cellCount == 3 && cellValue.equals(FOOD_CATEGORY_NAME)) {
                return cellValue;
            }
        }
        return currentValue;

    }

    private String getCategoryName(int cellCount, String cellValue, String currentValue) {
        if (cellCount == 2) {
            return cellValue;
        }
        if (!cellValue.equals("")) {
            if (cellCount == 3 && cellValue.equals(TOBACCO_SUBCATEGORY_NAME_IN_SHEETS)) {
                return TOBACCO_SUBCATEGORY_NAME_IN_SHEETS;
            } else if (cellCount == 3 && cellValue.equals(BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS)) {
                return BEVERAGES_SUBCATEGORY_NAME_IN_SHEETS;
            }
        }
        return currentValue;
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
