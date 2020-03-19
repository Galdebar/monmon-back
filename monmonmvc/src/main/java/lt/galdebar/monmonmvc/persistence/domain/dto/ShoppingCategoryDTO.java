package lt.galdebar.monmonmvc.persistence.domain.dto;

import lombok.*;

import java.util.Set;

@RequiredArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ShoppingCategoryDTO {
    @NonNull
    private String categoryName;
    @NonNull
    private Set<String> keywords;
}
