package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingItemEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface ShoppingItemRepo extends MongoRepository<ShoppingItemEntity, String> {
    ShoppingItemEntity findByItemName(String itemName);
    List<ShoppingItemEntity> findByItemCategory(String itemCategory);
    List<ShoppingItemEntity> findByItemCategoryAndUsers(String itemCategory, String user);
    List<ShoppingItemEntity> findByIsInCart(Boolean isInCart);
    List<ShoppingItemEntity> findByUsersIn(List<String> users);
}
