package lt.galdebar.monmonapi.app.api;

import lombok.extern.log4j.Log4j2;
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
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/lists")
@Log4j2
public class ShoppingListController {

    @Autowired
    private ShoppingListService service;

    @Autowired
    private ShoppingItemsController itemsController;

    @CrossOrigin
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StringResponse createList(@RequestBody ShoppingListDTO createRequest) {
        try {
            service.createList(createRequest);
        } catch (InvalidListRequest invalidRequest) {
            log.info(invalidRequest.getMessage());
            log.info(invalidRequest.getLocalizedMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidRequest.getMessage());
        }
        return new StringResponse("List created successfully");
    }

    @CrossOrigin
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

    @CrossOrigin
    @DeleteMapping("/delete")
    public StringResponse delete() {
        LocalDateTime deletionTime = service.markListForDeletion();
        return new StringResponse("List marked for deletion. Will be deleted at " + deletionTime + ". Deletion will be cancelled if logged in");
    }

    @CrossOrigin
    @PostMapping("/changepassword")
    public StringResponse changePassword(@RequestBody ChangePasswordRequest changeRequest) {
        try {
            service.changePassword(changeRequest);
        } catch (InvalidListRequest e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new StringResponse("Password changed successfully");
    }
}
