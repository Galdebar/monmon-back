package lt.galdebar.monmonapi.app.persistence.repositories;

import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShoppingListRepo extends CrudRepository<ShoppingListEntity, Long> {
    ShoppingListEntity findByNameIgnoreCase(String name);

    List<ShoppingListEntity> findByIsPendingDeletion(boolean isPendingDeletion);

    List<ShoppingListEntity> findByLastUsedTimeBefore(LocalDateTime lastUsedTime);

}
