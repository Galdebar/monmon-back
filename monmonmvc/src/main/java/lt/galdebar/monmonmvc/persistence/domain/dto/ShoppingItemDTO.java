package lt.galdebar.monmonmvc.persistence.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.HashSet;
import java.util.Set;


@AllArgsConstructor
@Getter
@Setter
@ToString
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
    public Set<String> users = new HashSet<>();


}
