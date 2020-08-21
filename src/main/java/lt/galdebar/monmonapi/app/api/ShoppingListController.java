package lt.galdebar.monmonapi.app.api;

import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ChangePasswordRequest;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.LoginAttemptDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListDTO;
import lt.galdebar.monmonapi.app.services.shoppinglists.ShoppingListService;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.InvalidListRequest;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.InvalidPassword;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.ListNameEmpty;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.ListNotFound;
import lt.galdebar.monmonapi.app.context.security.AuthTokenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/lists")
public class ShoppingListController {

    @Autowired
    private ShoppingListService service;

    @Autowired
    private ShoppingItemsController itemsController;

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
        } catch (ListNotFound notFound) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, notFound.getMessage());
        } catch (InvalidPassword | ListNameEmpty exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public String delete() {
        LocalDateTime deletionTime = service.markListForDeletion();
        return "List marked for deletion. Will be deleted at " + deletionTime + ". Deletion will be cancelled if logged in";
    }

    @PostMapping("/changepassword")
    public String changePassword(@RequestBody ChangePasswordRequest changeRequest) {
        try {
            service.changePassword(changeRequest);
        } catch (InvalidListRequest e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return "Password changed successfully";
    }
}
