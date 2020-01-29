package lt.galdebar.monmonmvc.dao;

import lt.galdebar.monmonmvc.model.Item;
import org.springframework.data.mongodb.repository.MongoRepository;


public interface MongoDBRepo extends MongoRepository<Item, String> {
}
