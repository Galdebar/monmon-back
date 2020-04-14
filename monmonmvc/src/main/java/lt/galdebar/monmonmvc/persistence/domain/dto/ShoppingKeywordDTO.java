package lt.galdebar.monmonmvc.persistence.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@AllArgsConstructor
@Getter
@Setter
@ToString
public class ShoppingKeywordDTO {
    private final String shoppingItemCategory;

    private final String keyword;
}
