package lt.galdebar.monmon.categoriesparser.persistence.domain;

import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class CategoryDTO {
    private String categoryName;
    private String subcategory;
    private String foodCategoryName;
    private Set<String> keywords;

    public CategoryDTO() {
        this.categoryName = "";
        this.subcategory = "";
        this.foodCategoryName = "";
        this.keywords = new HashSet<>();
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
