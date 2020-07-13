package lt.galdebar.monmonapi.services;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.LoginAttemptDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntityToDTOAdapter;
import lt.galdebar.monmonapi.persistence.repositories.ShoppingListRepo;
import lt.galdebar.monmonapi.services.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

import static java.time.LocalDateTime.*;

@Service
@RequiredArgsConstructor
public class ShoppingListService {
    private final ShoppingListRepo repo;
    private final PasswordEncoder passwordEncoder;
    private final ShoppingListEntityToDTOAdapter adapter;
    @Autowired
    private JwtTokenProvider tokenProvider;

    public ShoppingListDTO createList(LoginAttemptDTO createRequest) {
        checkIfCreateRequestValid(createRequest);
        ShoppingListEntity entity = new ShoppingListEntity();
        entity.setName(createRequest.getName());
        entity.setPassword(passwordEncoder.encode(createRequest.getPassword()));
        entity.setTimeCreated(now());
        entity.setLastUsedTime(now());

        ShoppingListEntity savedEntity = repo.save(entity);
        return adapter.entityToDTO(savedEntity);
    }

    public ShoppingListEntity findByListName(String listName) {
        ShoppingListEntity foundList = repo.findByNameIgnoreCase(listName);
        if (foundList == null) {
            throw new ListNotFound("List named " + listName + " not found");
        }
        return foundList;
    }

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

        return new AuthTokenDTO(foundList.getName(), token);
    }

    private void checkIfCreateRequestValid(LoginAttemptDTO createRequest) {
        if(createRequest.getPassword().trim().isEmpty() && createRequest.getName().trim().isEmpty()){
            throw new InvalidCreateListRequest("List name and password fiends cannot be empty.");
        }
        if (createRequest.getName().trim().isEmpty()) {
            throw new InvalidCreateListRequest("List name cannot be empty");
        }
        if(createRequest.getPassword().trim().isEmpty()){
            throw new InvalidCreateListRequest("Password cannot be empty");
        }
        if (repo.findByNameIgnoreCase(createRequest.getName()) != null) {
            throw new ListAlreadyExists(createRequest.getName());
        }
    }

    private void checkIfLoginRequestvalid(LoginAttemptDTO request){
        if(request.getName().trim().isEmpty()){
            throw new ListNameEmpty();
        }
        if(request.getPassword().trim().isEmpty()){
            throw new InvalidPassword("Password field empty");
        }
    }
}
