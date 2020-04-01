package lt.galdebar.monmonmvc.persistence.domain.dao;

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
@Table(name = "keywords")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class ShoppingKeywordDAO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @JsonManagedReference
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private ShoppingCategoryDAO shoppingItemCategory;
    @Field
    @Column(name = "keywords")
    private String keyword;


}
