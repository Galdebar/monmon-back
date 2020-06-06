package lt.galdebar.monmon.categoriesparser.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import java.util.Set;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@EqualsAndHashCode
public class ShoppingCategoryDTO {
    @NonNull
    private String categoryName;
    @NonNull
    @JsonIgnore
    private Set<String> keywords;
}
