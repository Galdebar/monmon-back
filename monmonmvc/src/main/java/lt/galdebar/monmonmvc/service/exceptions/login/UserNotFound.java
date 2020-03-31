package lt.galdebar.monmonmvc.service.exceptions.login;

import lombok.RequiredArgsConstructor;

public class UserNotFound extends LoginException {
    private static final String MESSAGE = "User not found: ";

    public UserNotFound(String userEmail) {
        super(userEmail, MESSAGE);
    }
}
