package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.dao.token.UserConnectionTokenDAO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserConnectionTokenRepo extends MongoRepository<UserConnectionTokenDAO, String> {
    UserConnectionTokenDAO findByToken(String token);
}
