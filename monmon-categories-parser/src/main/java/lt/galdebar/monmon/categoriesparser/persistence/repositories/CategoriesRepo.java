package lt.galdebar.monmon.categoriesparser.persistence.repositories;

import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriesRepo extends JpaRepository<ShoppingCategoryEntity, Long> {
}
