package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.entities.token.UserRegistrationTokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRegistrationTokenRepo extends MongoRepository <UserRegistrationTokenEntity, String> {
    UserRegistrationTokenEntity findByToken(String token);
}
