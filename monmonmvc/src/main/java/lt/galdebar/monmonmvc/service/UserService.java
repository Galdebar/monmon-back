package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

//    UserDTO getUser(UserDTO userDTO){
//        return daoToDto(
//
//        );
//    }

    public void addUser(UserDTO userDTO){
        userRepo.insert(dtoToDao(userDTO));
    }

    UserDTO daoToDto (UserDAO userDAO){
        return new UserDTO(userDAO.getUserName(), userDAO.getUserPassword());
    }

    UserDAO dtoToDao(UserDTO userDTO){
        UserDAO newUser = new UserDAO();
        newUser.setUserName(userDTO.getUserName());
        newUser.setUserPassword(userDTO.getUserPassword());
        return newUser;
    }
}
