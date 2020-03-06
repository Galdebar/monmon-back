package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public UserDTO addUser(UserDTO userDTO) {
        UserDAO newUser = new UserDAO();
        newUser.setUserEmail(userDTO.getUserEmail());
        newUser.setUserPassword(passwordEncoder.encode(userDTO.getUserPassword()));
        UserDAO addedUserDAO = userRepo.insert(newUser);
        return daoToDto(addedUserDAO);
    }

    public UserDTO findByUserEmail(String userEmail) {
        return daoToDto(userRepo.findByUserEmail(userEmail));
    }

    public UserDTO connectUserWithCurrent(UserDTO userToConnect) {
        UserDAO currentUser = userRepo.findByUserEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        currentUser.getConnectedUsers().add(userToConnect.getUserEmail());

        return daoToDto(userRepo.save(currentUser));
    }

    UserDTO daoToDto(UserDAO userDAO) {
        if (userDAO == null) {
            return new UserDTO();
        }
        return new UserDTO(userDAO.getUserEmail(), userDAO.getUserPassword());
    }

    public List<String> getConnectedUsers() {
        UserDAO currentUser = getCurrentUserDAO();
        List<String> connectedUserNames = new ArrayList<>();
        for(String userName: currentUser.getConnectedUsers()){
            if(userName != null){
                connectedUserNames.add(userName);
            }
        }

        return connectedUserNames;
    }

    private UserDAO getCurrentUserDAO() {
        return userRepo.findByUserEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    UserDAO dtoToDao(UserDTO userDTO) {
        UserDAO newUser = new UserDAO();
        newUser.setUserEmail(userDTO.getUserEmail());
        newUser.setUserPassword(userDTO.getUserPassword());
        return newUser;
    }
}
