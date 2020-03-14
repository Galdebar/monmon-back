package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.dao.UserEmailChangeTokenDAO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserEmailChangeTokenRepo extends MongoRepository<UserEmailChangeTokenDAO, String> {
    UserEmailChangeTokenDAO findByToken(String token);
}
