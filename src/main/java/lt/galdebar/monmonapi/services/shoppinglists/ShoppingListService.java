package lt.galdebar.monmonapi.services.shoppinglists;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.LoginAttemptDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.persistence.repositories.ShoppingListRepo;
import lt.galdebar.monmonapi.services.shoppinglists.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@RequiredArgsConstructor
public class ShoppingListService {
    private final ShoppingListRepo repo;
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenProvider tokenProvider;

    public ShoppingListDTO createList(ShoppingListDTO createRequest) {
        ShoppingListEntity entityToSave = new ShoppingListEntity(createRequest);
        entityToSave.setPassword(
                passwordEncoder.encode(entityToSave.getPassword())
        );

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

    public ShoppingListEntity getCurrentList(){
        return repo.findByNameIgnoreCase(
                SecurityContextHolder.getContext().getAuthentication().getName()
        );
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
