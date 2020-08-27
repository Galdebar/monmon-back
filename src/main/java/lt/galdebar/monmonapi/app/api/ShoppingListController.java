package lt.galdebar.monmonapi.app.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import lt.galdebar.monmonapi.app.context.security.jwt.JwtTokenProvider;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ChangePasswordRequest;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.LoginAttemptDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListDTO;
import lt.galdebar.monmonapi.app.services.blacklistedtokens.BlacklistedTokenService;
import lt.galdebar.monmonapi.app.services.shoppinglists.ShoppingListService;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.InvalidListRequest;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.InvalidPassword;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.ListNameEmpty;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.ListNotFound;
import lt.galdebar.monmonapi.app.context.security.AuthTokenDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/lists")
@Log4j2
@RequiredArgsConstructor
public class ShoppingListController implements GetUsernameFromSecurityContext{

    private final ShoppingListService service;
    private final ShoppingItemsController itemsController;
    private final BlacklistedTokenService tokenService;
    private final JwtTokenProvider tokenProvider;

    @CrossOrigin
    @PostMapping(value = "/create", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public StringResponse createList(@RequestBody ShoppingListDTO createRequest) {
        try {
            log.info(
                    "Creating list: " + createRequest.getName()
            );
            service.createList(createRequest);
        } catch (InvalidListRequest invalidRequest) {
            log.warn("Error creating list. " + invalidRequest.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, invalidRequest.getMessage());
        }
        return new StringResponse("List created successfully");
    }

    @CrossOrigin
    @PostMapping("/login")
    public AuthTokenDTO login(@RequestBody LoginAttemptDTO loginRequest) {
        log.info(
                "Logging in: " + loginRequest.getName()
        );

        try {
            return service.login(loginRequest);
        } catch (ListNotFound notFound) {
            log.warn("Error logging in. " + notFound.getMessage());
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, notFound.getMessage());
        } catch (InvalidPassword | ListNameEmpty exception) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, exception.getMessage());
        }
    }

    @CrossOrigin
    @GetMapping("/logout")
    public StringResponse logout(HttpServletRequest request) {
        log.info(
                "Logging out: " + getUserName()
        );

        invalidateSession(request);
        return new StringResponse("Logged out");
    }

    @CrossOrigin
    @DeleteMapping("/delete")
    public StringResponse delete(HttpServletRequest request) {
        log.info(
                "Markinig list for deletion: " + getUserName()
        );

        LocalDateTime deletionTime = service.markListForDeletion();
        invalidateSession(request);
        return new StringResponse("List marked for deletion. Will be deleted at " + deletionTime + ". Deletion will be cancelled if logged in again.");
    }

    @CrossOrigin
    @PostMapping("/changepassword")
    public StringResponse changePassword(@RequestBody ChangePasswordRequest changeRequest, HttpServletRequest request) {
        log.info("Changing password for list: " + getUserName()
        );
        try {
            service.changePassword(changeRequest);
            invalidateSession(request);
        } catch (InvalidListRequest e) {
            log.warn("Error changing password. " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
        return new StringResponse("Password changed successfully");
    }

    private void invalidateSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        SecurityContextHolder.clearContext();

        session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                cookie.setMaxAge(0);
            }
        }

        String token = tokenProvider.resolveToken(request);
        tokenService.addToken(token);

    }
}
