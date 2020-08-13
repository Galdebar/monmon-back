package lt.galdebar.monmonapi.categoriesparser.persistence.domain;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.search.annotations.Field;
import org.hibernate.search.annotations.Indexed;

import javax.persistence.*;

@Entity
@Indexed
@Table(name = "keywords")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ShoppingKeywordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonManagedReference
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private ShoppingCategoryEntity shoppingItemCategory;
    @Field
    @Column(name = "keywords")
    private String keyword;


}
