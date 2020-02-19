package lt.galdebar.monmonmvc.model.category;

import javax.persistence.*;
import java.util.Set;

@Entity
@Table(name = "categories")
public class NewCategoryDAO {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String categoryName;

    @ElementCollection
    @CollectionTable(name = "keywords", joinColumns = @JoinColumn(name = "category_id"))
    @Column
    private Set<String> keywords;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
}
