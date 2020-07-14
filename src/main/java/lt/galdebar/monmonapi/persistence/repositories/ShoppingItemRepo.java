package lt.galdebar.monmonapi.persistence.repositories;

import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingItemRepo extends CrudRepository<ShoppingItemEntity, Long> {
}
