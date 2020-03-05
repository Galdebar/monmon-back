package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO addUser(UserDTO userDTO){
        UserDAO newUser = new UserDAO();
        newUser.setUserEmail(userDTO.getUserEmail());
        newUser.setUserPassword(passwordEncoder.encode(userDTO.getUserPassword()));
        UserDAO addedUserDAO = userRepo.insert(newUser);
        return daoToDto(addedUserDAO);
    }

    public UserDTO findByUserEmail(String userEmail){
        return daoToDto(userRepo.findByUserEmail(userEmail));
    }

    UserDTO daoToDto (UserDAO userDAO){
        return new UserDTO(userDAO.getUserEmail(), userDAO.getUserPassword());
    }

    UserDAO dtoToDao(UserDTO userDTO){
        UserDAO newUser = new UserDAO();
        newUser.setUserEmail(userDTO.getUserEmail());
        newUser.setUserPassword(userDTO.getUserPassword());
        return newUser;
    }
}
