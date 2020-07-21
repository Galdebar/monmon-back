package lt.galdebar.monmonapi.services.shoppinglists.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

public class InvalidListRequest extends RuntimeException {
    public InvalidListRequest(String message){
        super(message);
    }
}
