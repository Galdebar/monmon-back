package lt.galdebar.monmonmvc.service.exceptions.registration;

public class TokenExpired extends UserRegistrationException {
    private static final String MESSAGE = "Registration token expired. ";
    private final String tokenID;

    public TokenExpired(String userEmail, String tokenID) {
        super(MESSAGE, userEmail);
        this.tokenID = tokenID;
    }

    @Override
    public String getMessage() {
        if(tokenID != null){
            String mainMessage =  super.getMessage();
            String details = String.format("Token ID: %s. ", tokenID);
            return mainMessage + details;
        } else return super.getMessage();
    }
}
