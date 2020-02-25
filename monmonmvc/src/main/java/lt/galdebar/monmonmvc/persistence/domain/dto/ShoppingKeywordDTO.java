package lt.galdebar.monmonmvc.persistence.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class ShoppingKeywordDTO {
    private final String shoppingItemCategory;

    private final String keyword;
}
