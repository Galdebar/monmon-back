package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.entities.token.LinkUsersTokenEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LinkUsersTokenRepo extends MongoRepository<LinkUsersTokenEntity, String> {
    LinkUsersTokenEntity findByToken(String token);
}
