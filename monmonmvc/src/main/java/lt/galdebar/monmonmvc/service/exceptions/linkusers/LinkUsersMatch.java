package lt.galdebar.monmonmvc.service.exceptions.linkusers;

import lombok.RequiredArgsConstructor;

public class LinkUsersMatch extends LinkUsersException {
    private static final String MESSAGE = "Users match. ";


    public LinkUsersMatch(String currentUser, String userToLink) {
        super(MESSAGE, currentUser, userToLink);
    }
}
