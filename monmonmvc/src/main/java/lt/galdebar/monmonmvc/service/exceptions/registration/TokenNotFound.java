package lt.galdebar.monmonmvc.service.exceptions.registration;

public class TokenNotFound extends UserRegistrationException {
    private static final String MESSAGE = "Token not found. ";
    private final String tokenID;

    public TokenNotFound(String tokenID) {
        super(MESSAGE, "NA");
        this.tokenID = tokenID;
    }

    @Override
    public String getMessage() {
        if(tokenID != null){
            String mainMessage = super.getMessage();
            String details = String.format("Attempting to find byID: %s. ", tokenID);
            return mainMessage + details;
        } else return super.getMessage();
    }
}
