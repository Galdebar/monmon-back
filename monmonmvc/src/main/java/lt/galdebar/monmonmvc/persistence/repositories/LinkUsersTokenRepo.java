package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.dao.token.LinkUsersTokenDAO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface LinkUsersTokenRepo extends MongoRepository<LinkUsersTokenDAO, String> {
    LinkUsersTokenDAO findByToken(String token);
}
