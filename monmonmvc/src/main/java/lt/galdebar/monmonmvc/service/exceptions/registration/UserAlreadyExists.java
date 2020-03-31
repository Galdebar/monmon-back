package lt.galdebar.monmonmvc.service.exceptions.registration;

public class UserAlreadyExists extends UserRegistrationException {
    private static final String MESSAGE = "User already exists. ";

    public UserAlreadyExists(String userEmail) {
        super(MESSAGE, userEmail);
    }
}
