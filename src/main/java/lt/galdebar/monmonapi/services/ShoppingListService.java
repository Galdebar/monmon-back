package lt.galdebar.monmonapi.services;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.LoginAttemptDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntityToDTOAdapter;
import lt.galdebar.monmonapi.persistence.repositories.ShoppingListRepo;
import lt.galdebar.monmonapi.services.exceptions.InvalidPassword;
import lt.galdebar.monmonapi.services.exceptions.ListNotFound;
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
        ShoppingListEntity foundList = repo.findByNameIgnoreCase(loginRequest.getName());
        if(foundList==null){
            throw new ListNotFound("List named " + loginRequest.getName() + " not found");
        }
        if(!passwordEncoder.matches(loginRequest.getPassword(),foundList.getPassword())){
            throw new InvalidPassword("Invalid password");
        }

        String token = tokenProvider.createToken(
                foundList.getName(),
                Collections.singletonList(foundList.toString())
        );

        AuthTokenDTO tokenDTO = new AuthTokenDTO(foundList.getName(),token);

        return tokenDTO;
    }
}
