package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.dao.UserRegistrationTokenDAO;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRegistrationTokenRepo extends MongoRepository <UserRegistrationTokenDAO, String> {
    public UserRegistrationTokenDAO findByToken(String token);
}
