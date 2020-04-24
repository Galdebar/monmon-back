package lt.galdebar.monmonscraper.persistence.dao;

import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ShoppingItemDealsRepo extends MongoRepository<ShoppingItemDealEntity, String> {
}
