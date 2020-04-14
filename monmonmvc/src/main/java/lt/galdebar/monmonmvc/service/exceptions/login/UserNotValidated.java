package lt.galdebar.monmonmvc.service.exceptions.login;

import lombok.RequiredArgsConstructor;

public class UserNotValidated extends LoginException {
    private static final String MESSAGE = "User not validated: ";

    public UserNotValidated(String userEmail) {
        super(userEmail, MESSAGE);
    }
}
