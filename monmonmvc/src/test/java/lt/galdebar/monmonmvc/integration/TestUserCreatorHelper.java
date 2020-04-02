package lt.galdebar.monmonmvc.integration;

import lt.galdebar.monmonmvc.persistence.domain.entities.UserEntity;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import org.springframework.security.crypto.password.PasswordEncoder;

public class TestUserCreatorHelper {

    private UserRepo userRepo;

    private PasswordEncoder passwordEncoder;

    TestUserCreatorHelper(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    UserEntity createSimpleUser(String userEmail, String userPassword) throws UserNotFound {
        if(userRepo.findByUserEmail(userEmail) == null){
            UserEntity user = new UserEntity();
            user.setUserEmail(userEmail);
            user.setUserPassword(passwordEncoder.encode(userPassword));
            user.setValidated(true);
            return userRepo.save(user);
        } else return userRepo.findByUserEmail(userEmail);
    }

    UserEntity createLinkedUsers(String userA, String userB) throws UserNotFound {
        UserEntity user = userRepo.findByUserEmail(userA);
        UserEntity userToAdd = userRepo.findByUserEmail(userB);

        if(user == null || userToAdd == null){
            throw new UserNotFound(user.getUserEmail());
        }

        user.getLinkedUsers().add(userToAdd.getUserEmail());
        userToAdd.getLinkedUsers().add(user.getUserEmail());
        userRepo.save(userToAdd);
        return userRepo.save(user);
    }

    void clearDB(){
        userRepo.deleteAll();
    }
}
