package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingItemDAO;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;


public interface ShoppingItemRepo extends MongoRepository<ShoppingItemDAO, String> {
    ShoppingItemDAO findByItemName(String itemName);
    List<ShoppingItemDAO> findByItemCategory(String itemCategory);
    List<ShoppingItemDAO> findByItemCategoryAndUsers(String itemCategory, String user);
    List<ShoppingItemDAO> findByIsInCart(Boolean isInCart);
    List<ShoppingItemDAO> findByUsersIn(List<String> users);
}
