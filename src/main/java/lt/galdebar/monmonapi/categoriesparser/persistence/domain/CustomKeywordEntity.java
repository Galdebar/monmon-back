package lt.galdebar.monmonapi.categoriesparser.persistence.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Entity
@Indexed
@Table(name = "custom_keywords")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CustomKeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonManagedReference
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private ShoppingCategoryEntity shoppingItemCategory;
    @Field
    @Column(name = "custom_keywords",columnDefinition="TEXT")
    private String customKeyword;

    public CustomKeywordDTO getDTO(){
        return new CustomKeywordDTO(
                shoppingItemCategory.getCategoryName(),
                customKeyword
        );
    }

    public ShoppingKeywordDTO getStandardKeywordDTO(){
        return new ShoppingKeywordDTO(
                shoppingItemCategory.getCategoryName(),
                customKeyword
        );
    }


}
