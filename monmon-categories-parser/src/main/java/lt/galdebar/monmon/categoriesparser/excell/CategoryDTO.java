package lt.galdebar.monmon.categoriesparser.excell;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public class CategoryDTO {
    private String categoryName;
    private String subcategory;
    private String foodCategoryName;
    private Set<String> keywords;

    public CategoryDTO() {
    }

    public CategoryDTO(String categoryName, String subcategory, String foodCategoryName, Set<String> keywords) {
        this.categoryName = categoryName;
        this.subcategory = subcategory;
        this.foodCategoryName = foodCategoryName;
        this.keywords = keywords;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public String getSubcategory() {
        return subcategory;
    }

    public String getFoodCategoryName() {
        return foodCategoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setSubcategory(String subcategory) {
        this.subcategory = subcategory;
    }

    public void setFoodCategoryName(String foodCategoryName) {
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
