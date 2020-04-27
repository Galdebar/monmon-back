package lt.galdebar.monmonscraper.persistence.dao;

import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ShoppingItemDealsRepo extends MongoRepository<ShoppingItemDealEntity, String> {
    List<ShoppingItemDealEntity> findByShopTitle(String shopTitle);
    List<ShoppingItemDealEntity> findByItemKeyword(String itemKeyword);
}
