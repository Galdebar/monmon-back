package lt.galdebar.monmonapi.categoriesparser.persistence.domain;

import lombok.*;

import java.util.Set;

//@RequiredArgsConstructor
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@ToString
public class CategoryDTO {
//    @NonNull
    private String categoryName;
//    @NonNull
    private Set<String> keywords;
}
