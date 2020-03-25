package lt.galdebar.monmon.categoriesparser.persistence.repositories;

import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDAO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KeywordsRepo extends JpaRepository<KeywordDAO, Long> {
}
