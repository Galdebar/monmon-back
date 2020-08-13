package lt.galdebar.monmonapi.categoriesparser.persistence.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;
import java.util.Set;

@Entity
@Indexed
@Table(name = "categories")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ShoppingCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Field(name = "category_name")
    @Column(unique = true, name = "category_name")
    private String categoryName;
    @JsonBackReference
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "shoppingItemCategory", fetch = FetchType.EAGER)
    private Set<ShoppingKeywordEntity> keywords;

    public ShoppingCategoryEntity(String categoryName, Set<ShoppingKeywordEntity> keywords) {
        this.categoryName = categoryName;
        this.keywords = keywords;
    }
}
