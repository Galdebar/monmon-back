package lt.galdebar.monmonapi.persistence.repositories;

import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemEntity;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShoppingItemRepo extends CrudRepository<ShoppingItemEntity, Long> {
    List<ShoppingItemEntity> findByShoppingList(ShoppingListEntity shoppingList);
}
