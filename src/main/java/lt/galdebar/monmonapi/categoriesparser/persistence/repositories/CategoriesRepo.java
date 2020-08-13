package lt.galdebar.monmonapi.categoriesparser.persistence.repositories;

import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoriesRepo extends JpaRepository<ShoppingCategoryEntity, Long> {
}
