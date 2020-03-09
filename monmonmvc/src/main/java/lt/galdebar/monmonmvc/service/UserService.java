package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.AuthTokenDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.LoginAttemptDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.UserDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyExists;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotValidated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;


    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public AuthTokenDTO login(LoginAttemptDTO loginAttemptDTO) throws UserNotValidated, UserNotFound {
        UserDAO foundUser = loginUserCheck(loginAttemptDTO);
        String token = jwtTokenProvider.createToken(
                foundUser.getUserEmail(),
                Collections.singletonList(foundUser.toString())
        );

        AuthTokenDTO tokenDTO = new AuthTokenDTO(
                foundUser.getUserEmail(),
                token
        );
        return tokenDTO;
    }

    private UserDAO loginUserCheck(LoginAttemptDTO loginAttemptDTO) throws UserNotFound, UserNotValidated {
        UserDAO foundUser = userRepo.findByUserEmail(loginAttemptDTO.getUserEmail());
        if (foundUser == null) {
            throw new UserNotFound();
        }
        if (!foundUser.isValidated()) {
            throw new UserNotValidated();
        }
        return foundUser;
    }

    public UserDAO registerUser(UserDTO userDTO) throws UserAlreadyExists {
        if (checkIfUserExists(userDTO)) {
            throw new UserAlreadyExists();
        }
        UserDAO newUser = new UserDAO();
        newUser.setUserEmail(userDTO.getUserEmail());
        newUser.setUserPassword(passwordEncoder.encode(userDTO.getUserPassword()));
        newUser.setValidated(false);
        UserDAO addedUserDAO = userRepo.insert(newUser);
        return addedUserDAO;
    }

    private boolean checkIfUserExists(UserDTO userDTO) {
        return userRepo.findByUserEmail(userDTO.getUserEmail()) != null;
    }

    public UserDTO findByUserEmail(String userEmail) {
        return daoToDto(userRepo.findByUserEmail(userEmail));
    }

    public UserDTO connectUserWithCurrent(UserDTO userToConnect) {
        UserDAO currentUser = userRepo.findByUserEmail(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );

        currentUser.getConnectedUsers().add(userToConnect.getUserEmail());
        UserDAO userToConnectDAO = userRepo.findByUserEmail(userToConnect.getUserEmail());
        userToConnectDAO.getConnectedUsers().add(currentUser.getUserEmail());
        userRepo.save(userToConnectDAO);

        return daoToDto(userRepo.save(currentUser));
    }

    private UserDTO daoToDto(UserDAO userDAO) {
        if (userDAO == null) {
            return new UserDTO();
        }
        return new UserDTO(userDAO.getUserEmail(), userDAO.getUserPassword());
    }

    public List<String> getConnectedUsers() {
        UserDAO currentUser = getCurrentUserDAO();
        List<String> connectedUserNames = new ArrayList<>();
        for (String userName : currentUser.getConnectedUsers()) {
            if (userName != null) {
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

    public boolean validateUser(UserDAO user) {
        user.setValidated(true);
        return userRepo.save(user) != null;
    }
}
