package lt.galdebar.monmonmvc.service.exceptions.linkusers;

import lt.galdebar.monmonmvc.persistence.domain.dao.token.LinkUsersTokenDAO;

public class LinkUsersTokenExpired extends LinkUsersException {
    private static final String MESSAGE = "Link users token expired. ";
    private final String tokenId;

    public LinkUsersTokenExpired(String currentUser, String userToLink, String tokenId) {
        super(MESSAGE, currentUser, userToLink);
        this.tokenId = tokenId;
    }

    @Override
    public String getMessage() {
        String mainMessage =  super.getMessage();
        String tokenDetails = String.format("Tokeni ID: %s. ", tokenId);
        return mainMessage + tokenDetails;
    }
}
