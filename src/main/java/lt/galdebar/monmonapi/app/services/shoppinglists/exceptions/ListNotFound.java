package lt.galdebar.monmonapi.app.services.shoppinglists.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//@ResponseStatus(HttpStatus.NOT_FOUND)
public class ListNotFound extends RuntimeException {
    public ListNotFound(String message){
        super(message);
    }
}
