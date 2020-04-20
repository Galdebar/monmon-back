package lt.galdebar.monmon.categoriesparser.persistence.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class KeywordDTO {
    private final String shoppingItemCategory;

    private final String keyword;
}
