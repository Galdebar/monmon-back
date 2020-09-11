package lt.galdebar.monmonapi.app.services.shoppingdeals.exceptions;

public class BadDealRequest extends RuntimeException {
    public BadDealRequest(String message){
        super(message);
    }
}
