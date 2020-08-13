package lt.galdebar.monmonapi.app.persistence.repositories;

import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemEntity;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingItemRepo extends CrudRepository<ShoppingItemEntity, Long> {
    List<ShoppingItemEntity> findByShoppingList(ShoppingListEntity shoppingList);
}
