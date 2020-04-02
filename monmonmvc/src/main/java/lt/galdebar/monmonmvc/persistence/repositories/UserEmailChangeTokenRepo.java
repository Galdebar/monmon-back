package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.entities.token.UserEmailChangeTokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserEmailChangeTokenRepo extends MongoRepository<UserEmailChangeTokenEntity, String> {
    UserEmailChangeTokenEntity findByToken(String token);
}
