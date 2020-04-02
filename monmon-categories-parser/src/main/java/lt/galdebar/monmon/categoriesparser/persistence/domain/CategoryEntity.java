package lt.galdebar.monmon.categoriesparser.persistence.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class CategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true, name = "category_name")
    private String categoryName;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name="category_id",referencedColumnName="id")
    private Set<KeywordEntity> keywords;


    public CategoryEntity(String categoryName, Set<KeywordEntity> keywords) {
        this.categoryName = categoryName;
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "CategoryEntity{" +
                "categoryName='" + categoryName + '\'' +
                ", keywords=" + keywords +
                '}';
    }


}
