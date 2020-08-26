package lt.galdebar.monmonapi.app.persistence.repositories;

import lt.galdebar.monmonapi.app.persistence.domain.jwtokens.BlacklistedTokenEntity;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemEntity;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListEntity;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BlacklistedTokenRepo extends CrudRepository<BlacklistedTokenEntity, Long> {
    List<BlacklistedTokenEntity> findByToken(String token);
    boolean existsBlacklistedTokenByToken(String token);
    List<BlacklistedTokenEntity> findByDateAddedBefore(LocalDateTime dateAdded);
}
