package lt.galdebar.monmonapi.app.services.shoppinglists;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ChangePasswordRequest;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.*;
import lt.galdebar.monmonapi.app.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.app.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.LoginAttemptDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.app.persistence.repositories.ShoppingListRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingListService {
    private final ShoppingListRepo repo;
    private final PasswordEncoder passwordEncoder;
    @Getter
    private final int LIST_DELETION_GRACE_PERIOD = 48;
    @Getter
    private final int PASSWORD_MIN_CHARACTERS = 5;
    @Autowired
    private JwtTokenProvider tokenProvider;

    public ShoppingListDTO createList(ShoppingListDTO createRequest) {
        checkPasswordLength(createRequest);
        ShoppingListEntity entityToSave = new ShoppingListEntity(createRequest);
        entityToSave.setPassword(
                passwordEncoder.encode(entityToSave.getPassword())
        );
        entityToSave.setTimeCreated(LocalDateTime.now());
        entityToSave.setLastUsedTime(LocalDateTime.now());

        if (repo.findByNameIgnoreCase(entityToSave.getName()) != null) {
            throw new ListAlreadyExists(createRequest.getName());
        }

        return repo.save(entityToSave).getDTO();
    }

    public ShoppingListEntity findByListName(String listName) {
        ShoppingListEntity foundList = repo.findByNameIgnoreCase(listName);
        if (foundList == null) {
            throw new ListNotFound("List named " + listName + " not found");
        }
        return foundList;
    }

    @Transactional
    public AuthTokenDTO login(LoginAttemptDTO loginRequest) {
        checkIfLoginRequestvalid(loginRequest);
        ShoppingListEntity foundList = repo.findByNameIgnoreCase(loginRequest.getName());
        if (foundList == null) {
            throw new ListNotFound("List named " + loginRequest.getName() + " not found");
        }
        if (!passwordEncoder.matches(loginRequest.getPassword(), foundList.getPassword())) {
            throw new InvalidPassword("Invalid password");
        }

        String token = tokenProvider.createToken(
                foundList.getName(),
                Collections.singletonList(foundList.toString())
        );
        foundList.setLastUsedTime(LocalDateTime.now());
        if(foundList.isPendingDeletion()){
            foundList.setPendingDeletion(false);
            foundList.setDeletionTime(null);
        }
        repo.save(foundList);


        return new AuthTokenDTO(foundList.getName(), token);
    }

    public ShoppingListEntity getCurrentList() {
        return repo.findByNameIgnoreCase(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
    }

    @Transactional
    public LocalDateTime markListForDeletion() {
        ShoppingListEntity list = getCurrentList();
        if (list.isPendingDeletion()) {
            return list.getDeletionTime();
        }

        list.setPendingDeletion(true);
        list.setDeletionTime(getListDeletionDate());
        repo.save(list);
        return list.getDeletionTime();
    }

    @Transactional
    public boolean deleteList() {
        repo.delete(getCurrentList());
        return true;
    }

    @Transactional
    public void deleteList(String listName) {
        ShoppingListEntity list = findByListName(listName);
        repo.delete(list);
    }

    public List<ShoppingListDTO> getListsPendingDeletion() {
        List<ShoppingListEntity> foundLists = repo.findByIsPendingDeletion(true);
        return foundLists.stream()
                .filter(shoppingListEntity -> shoppingListEntity.getDeletionTime().isBefore(LocalDateTime.now()))
                .map(ShoppingListEntity::getDTO)
                .collect(Collectors.toList());
    }

    public List<ShoppingListDTO> getOldLists(LocalDateTime cutoffDate) {
        List<ShoppingListEntity> foundLists = repo.findByLastUsedTimeBefore(
                cutoffDate
        );
        return foundLists.stream()
                .map(ShoppingListEntity::getDTO)
                .collect(Collectors.toList());
    }

    public void changePassword(ChangePasswordRequest changeRequest) {
        checkIfPasswordChangeRequestValid(changeRequest);
        ShoppingListEntity currentList = getCurrentList();
        currentList.setPassword(
                passwordEncoder.encode(changeRequest.getNewPassword())
        );
        repo.save(currentList);
    }

    private void checkIfLoginRequestvalid(LoginAttemptDTO request) {
        if(request == null) {
            throw new InvalidListRequest("Request cannot be empty or is formatted incorrectly");
        }
        if (request.getName().trim().isEmpty()) {
            throw new ListNameEmpty();
        }
        if (request.getPassword().trim().isEmpty()) {
            throw new InvalidPassword("Password field empty");
        }
    }

    private void checkPasswordLength(ShoppingListDTO createRequest) {
        if(createRequest.getPassword().trim().length() < PASSWORD_MIN_CHARACTERS){
            throw new ListPasswordTooShort(PASSWORD_MIN_CHARACTERS);
        }
    }

    private void checkIfPasswordChangeRequestValid(ChangePasswordRequest changeRequest) {
        String currentPassword = getCurrentList().getPassword();
        if (changeRequest == null ||
                ((changeRequest.getOldPassword() == null || changeRequest.getOldPassword().trim().isEmpty()) &&
                        (changeRequest.getNewPassword() == null || changeRequest.getNewPassword().trim().isEmpty()))) {
            throw new InvalidListRequest("Request cannot be empty");
        }
        if (changeRequest.getOldPassword() == null || changeRequest.getOldPassword().trim().isEmpty()) {
            throw new InvalidListRequest("Old password field cannot be empty");
        }
        if (changeRequest.getNewPassword() == null || changeRequest.getNewPassword().trim().isEmpty()) {
            throw new InvalidListRequest("New password field cannot be empty");
        }
        if(!passwordEncoder.matches(changeRequest.getOldPassword(),currentPassword)){
            throw new InvalidListRequest("Old password doesn't match the current one");
        }
        if(passwordEncoder.matches(changeRequest.getNewPassword(),currentPassword)){
            throw new InvalidListRequest("New password must be different from the old one");
        }

    }

    private LocalDateTime getListDeletionDate() {
        return LocalDateTime.now().plusHours(LIST_DELETION_GRACE_PERIOD);
    }
}
