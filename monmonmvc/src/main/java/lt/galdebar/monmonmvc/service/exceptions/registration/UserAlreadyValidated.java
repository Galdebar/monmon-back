package lt.galdebar.monmonmvc.service.exceptions.registration;

public class UserAlreadyValidated extends UserRegistrationException {
    private static final String MESSAGE = "User already validated. ";

    public UserAlreadyValidated(String userEmail) {
        super(MESSAGE, userEmail);
    }
}
