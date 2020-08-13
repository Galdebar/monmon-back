package lt.galdebar.monmonapi.categoriesparser.persistence.repositories;

import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingKeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordsRepo extends JpaRepository<ShoppingKeywordEntity, Long> {
}
