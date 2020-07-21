package lt.galdebar.monmonapi.api;

import lt.galdebar.monmonapi.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.LoginAttemptDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListDTO;
import lt.galdebar.monmonapi.services.shoppinglists.ShoppingListService;
import lt.galdebar.monmonapi.services.shoppinglists.exceptions.InvalidListRequest;
import lt.galdebar.monmonapi.services.shoppinglists.exceptions.InvalidPassword;
import lt.galdebar.monmonapi.services.shoppinglists.exceptions.ListNameEmpty;
import lt.galdebar.monmonapi.services.shoppinglists.exceptions.ListNotFound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/lists")
public class ShoppingListController {

    @Autowired
    private ShoppingListService service;

    @PostMapping("/create")
    public String createList(@RequestBody ShoppingListDTO createRequest) {
        try {
            service.createList(createRequest);
        } catch (InvalidListRequest exists) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, exists.getMessage());
        }
        return "List created";
    }

    @PostMapping("/login")
    public AuthTokenDTO login(@RequestBody LoginAttemptDTO loginRequest) {
        try {
            return service.login(loginRequest);
        } catch (ListNotFound notFound){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, notFound.getMessage());
        } catch (InvalidPassword | ListNameEmpty exception){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }
}
