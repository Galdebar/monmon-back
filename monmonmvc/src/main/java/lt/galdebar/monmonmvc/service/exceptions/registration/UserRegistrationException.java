package lt.galdebar.monmonmvc.service.exceptions.registration;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonmvc.service.exceptions.CanSendResponse;

@RequiredArgsConstructor
public class UserRegistrationException extends Throwable implements CanSendResponse {
    private static final String PARENT_MESSAGE = "User registration exception || ";
    private final String specificMessage;
    private final String userEmail;

    @Override
    public String getMessage() {
        if(userEmail != null){
            return PARENT_MESSAGE + specificMessage + String.format("User: %s. ", userEmail);
        } else return PARENT_MESSAGE + specificMessage;
    }

    @Override
    public String getResponseMessage() {
        return specificMessage;
    }
}
