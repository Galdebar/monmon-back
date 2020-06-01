package lt.galdebar.monmon.categoriesparser.persistence.repositories;

import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordEntity;
import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingKeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordsRepo extends JpaRepository<ShoppingKeywordEntity, Long> {
}
