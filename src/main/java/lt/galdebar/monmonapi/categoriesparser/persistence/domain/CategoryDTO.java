package lt.galdebar.monmonapi.categoriesparser.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
    @JsonIgnore
    private Set<String> keywords;
}
