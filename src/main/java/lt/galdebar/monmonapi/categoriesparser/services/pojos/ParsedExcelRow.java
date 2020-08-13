package lt.galdebar.monmonapi.categoriesparser.services.pojos;

import lombok.*;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ParsedExcelRow {
    private String categoryName;
    private String subcategory;
    private String foodCategoryName;
    private Set<String> keywords;

    public ParsedExcelRow() {
        this.categoryName = "";
        this.subcategory = "";
        this.foodCategoryName = "";
        this.keywords = new HashSet<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ParsedExcelRow that = (ParsedExcelRow) o;
        return categoryName.equals(that.categoryName) &&
                Objects.equals(keywords, that.keywords);
    }
}
