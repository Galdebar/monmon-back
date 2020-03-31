package lt.galdebar.monmonmvc.service.exceptions.registration;

public class TokenNotExpired extends UserRegistrationException {
    private static final String MESSAGE = "Cannot extend token duration- token not expired. ";

    public TokenNotExpired(String userEmail) {
        super(MESSAGE, userEmail);
    }
}
