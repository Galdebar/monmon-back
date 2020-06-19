package lt.galdebar.monmonapi.persistence.repositories;

import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShoppingListRepo extends CrudRepository<ShoppingListEntity, Long> {
    ShoppingListEntity findByNameIgnoreCase(String name);
}
