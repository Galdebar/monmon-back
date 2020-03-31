package lt.galdebar.monmonmvc.service.exceptions.linkusers;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.service.exceptions.CanSendResponse;

@RequiredArgsConstructor
public class LinkUsersException extends Throwable implements CanSendResponse {
    private static final String PARENT_MESSAGE = "Link users exception ||";
    private final String specificMessage;
    private final String currentUser;
    private final String userToLink;

    @Override
    public String getMessage() {
        if(currentUser == null || userToLink == null){
            return  PARENT_MESSAGE + specificMessage;
        }else {
            String userDetails = String.format(
                    "Current user: %s, user to link: %s. ", currentUser, userToLink
            );
            return PARENT_MESSAGE + specificMessage + userDetails;
        }
    }

    @Override
    public String getResponseMessage() {
        return specificMessage;
    }
}
