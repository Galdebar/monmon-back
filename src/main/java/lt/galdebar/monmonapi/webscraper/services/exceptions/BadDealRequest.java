package lt.galdebar.monmonapi.webscraper.services.exceptions;

public class BadDealRequest extends RuntimeException {
    public BadDealRequest(String message){
        super(message);
    }
}
