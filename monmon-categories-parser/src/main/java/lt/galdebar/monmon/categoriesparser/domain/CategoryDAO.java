package lt.galdebar.monmon.categoriesparser.domain;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "categories")
public class CategoryDAO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
    @Column(unique = true)
    private String categoryName;
    @ElementCollection
    @CollectionTable(name = "keywords", joinColumns = @JoinColumn(name = "category_id"))
    @Column
    private Set<String> keywords;

    public CategoryDAO() {
    }

    public CategoryDAO(String categoryName, Set<String> keywords) {
        this.categoryName = categoryName;
        this.keywords = keywords;
    }

    public long getId() {
        return id;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Set<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(Set<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public String toString() {
        return "CategoryDAO{" +
                "categoryName='" + categoryName + '\'' +
                ", keywords=" + keywords +
                '}';
    }
}
