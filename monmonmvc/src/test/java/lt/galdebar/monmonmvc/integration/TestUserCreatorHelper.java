package lt.galdebar.monmonmvc.integration;

import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

public class TestUserCreatorHelper {

    private UserRepo userRepo;

    private PasswordEncoder passwordEncoder;

    public TestUserCreatorHelper(UserRepo userRepo, PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    public UserDAO createSimpleUser(String userEmail, String userPassword) throws UserNotFound {
        if(userRepo.findByUserEmail(userEmail) == null){
            UserDAO user = new UserDAO();
            user.setUserEmail(userEmail);
            user.setUserPassword(passwordEncoder.encode(userPassword));
            user.setValidated(true);
            return userRepo.save(user);
        } else return userRepo.findByUserEmail(userEmail);
    }

    public UserDAO createLinkedUsers(String userA, String userB) throws UserNotFound {
        UserDAO user = userRepo.findByUserEmail(userA);
        UserDAO userToAdd = userRepo.findByUserEmail(userB);

        if(user == null || userToAdd == null){
            throw new UserNotFound(user.getUserEmail());
        }

        user.getLinkedUsers().add(userToAdd.getUserEmail());
        userToAdd.getLinkedUsers().add(user.getUserEmail());
        userRepo.save(userToAdd);
        return userRepo.save(user);
    }

    public void clearDB(){
        userRepo.deleteAll();
    }
}
