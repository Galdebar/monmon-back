package lt.galdebar.monmonmvc.persistence.repositories;

import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface UserRepo extends MongoRepository<UserEntity, String> {
    UserEntity findByUserEmailIgnoreCase(String userEmail);
    List<UserEntity> findByToBeDeleted(boolean toBeDeleted);
    List<UserEntity> findByUserEmailIn(List<String> userEmail);
}
