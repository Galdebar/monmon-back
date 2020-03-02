package lt.galdebar.monmonmvc.persistence.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@AllArgsConstructor
@Getter
@Setter
public class ShoppingItemDTO {
    @JsonProperty
    private String id;
    @JsonProperty
    private String itemName;
    @JsonProperty
    private String itemCategory;
    @JsonProperty
    private Integer quantity;
    @JsonProperty
    private String comment;
    @JsonProperty("isInCart")
    private boolean isInCart;

}
