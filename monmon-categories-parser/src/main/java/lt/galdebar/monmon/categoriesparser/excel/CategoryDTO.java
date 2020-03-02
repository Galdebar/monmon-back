package lt.galdebar.monmon.categoriesparser.excel;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class CategoryDTO {
    private String categoryName;
    private String subcategory;
    private String foodCategoryName;
    private Set<String> keywords;

    CategoryDTO() {
        this.categoryName = "";
        this.subcategory = "";
        this.foodCategoryName = "";
        this.keywords = new HashSet<>();
    }

    CategoryDTO(String categoryName, String subcategory, String foodCategoryName, Set<String> keywords) {
        this.categoryName = categoryName;
        this.subcategory = subcategory;
        this.foodCategoryName = foodCategoryName;
        this.keywords = keywords;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    String getSubcategory() {
        return subcategory;
    }

    String getFoodCategoryName() {
        return foodCategoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    void setFoodCategoryName(String foodCategoryName) {
        this.foodCategoryName = foodCategoryName;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "CategoryDTO{" +
                "categoryName='" + categoryName + '\'' +
                ", subcategory='" + subcategory + '\'' +
                ", foodCategoryName='" + foodCategoryName + '\'' +
                ", keywords=" + keywords +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CategoryDTO that = (CategoryDTO) o;
        return categoryName.equals(that.categoryName) &&
                Objects.equals(keywords, that.keywords);
    }

    @Override
    public int hashCode() {
        return Objects.hash(categoryName, keywords);
    }
}
