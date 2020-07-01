package lt.galdebar.monmonapi.services.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

public class ListAlreadyExists extends InvalidCreateListRequest {
    public ListAlreadyExists(String listName){
        super("List name " + listName + " is already taken");
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
