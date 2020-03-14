package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserConnectionTokenDAO;
import lt.galdebar.monmonmvc.persistence.domain.dao.UserDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.*;
import lt.galdebar.monmonmvc.persistence.repositories.UserConnectionTokenRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import lt.galdebar.monmonmvc.service.exceptions.registration.UserAlreadyExists;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotValidated;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;

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

    @Transactional
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
        if(!passwordEncoder.matches(loginAttemptDTO.getUserPassword(),foundUser.getUserPassword())){
            throw new BadCredentialsException("Invalid password");
        }
        return foundUser;
    }

    @Transactional
    public UserDAO registerUser(UserDTO userDTO) throws UserAlreadyExists {
        if (checkIfUserExists(userDTO.getUserEmail())) {
            throw new UserAlreadyExists();
        }
        UserDAO newUser = new UserDAO();
        newUser.setUserEmail(userDTO.getUserEmail());
        newUser.setUserPassword(passwordEncoder.encode(userDTO.getUserPassword()));
        newUser.setValidated(false);
        UserDAO addedUserDAO = userRepo.insert(newUser);
        return addedUserDAO;
    }

    public boolean checkIfUserExists(String userEmail) {
        return userRepo.findByUserEmail(userEmail) != null;
    }

    public UserDTO findByUserEmail(String userEmail) {
        return daoToDto(userRepo.findByUserEmail(userEmail));
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

    public boolean validateUser(UserDAO user) {
        user.setValidated(true);
        return userRepo.save(user) != null;
    }

    @Transactional
    public void changeEmail(EmailChangeRequest emailChangeRequest) throws UserAlreadyExists {
        if (!checkIfUserExists(emailChangeRequest.getNewEmail())) {
            UserDAO currentUser = getCurrentUserDAO();
            currentUser.setUserEmail(emailChangeRequest.getNewEmail());
            userRepo.save(currentUser);
        } else throw new UserAlreadyExists();

    }

    @Transactional
    public void changePassword(PasswordChangeRequest passwordChangeRequest) throws BadCredentialsException {
        UserDAO currentUser = getCurrentUserDAO();
        if(!currentUser.getUserEmail().equals(passwordChangeRequest.getUserEmail())){
            throw new BadCredentialsException("Invalid user email");
        }

        if (passwordEncoder.matches(passwordChangeRequest.getOldPassword(), currentUser.getUserPassword())) {
            currentUser.setUserPassword(
                    passwordEncoder.encode(
                            passwordChangeRequest.getNewPassword()
                    )
            );
            userRepo.save(currentUser);
        } else throw new BadCredentialsException("Invalid password supplied");
    }

    public boolean updateUserEmail(UserDAO user, String newEmail) {
        user.setUserEmail(newEmail);
        return userRepo.save(user) != null;
    }

    public UserDAO updateUser(UserDAO currentUser) {
        return userRepo.save(currentUser);
    }

    public UserDAO getCurrentUserDAO() {
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
