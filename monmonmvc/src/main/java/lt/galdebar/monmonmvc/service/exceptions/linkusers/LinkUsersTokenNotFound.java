package lt.galdebar.monmonmvc.service.exceptions.linkusers;

public class LinkUsersTokenNotFound extends LinkUsersException {
    private static final String MESSAGE = "Link users token not found. ";
    private final String tokenId;

    public LinkUsersTokenNotFound(String tokenId) {
        super(MESSAGE, "NA", "NA");
        this.tokenId = tokenId;
    }

    @Override
    public String getMessage() {
        String mainMessage =  super.getMessage();
        String tokenDetails = String.format("Attempted to find token by ID: %s. ", tokenId);
        return mainMessage + tokenDetails;
    }
}
