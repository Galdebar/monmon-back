package lt.galdebar.monmonmvc.persistence.domain.dto;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.Set;

@RequiredArgsConstructor
@Getter
@Setter
public class ShoppingCategoryDTO {
    @NonNull
    private final String categoryName;
    @NonNull
    private final Set<String> keywords;
}
