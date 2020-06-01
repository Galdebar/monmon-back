package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingCategoryEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ItemCategoryRepo extends CrudRepository<ShoppingCategoryEntity, Long> {
}
