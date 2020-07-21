package lt.galdebar.monmonapi.services.shoppinglists.exceptions;

public class ListAlreadyExists extends InvalidListRequest {
    public ListAlreadyExists(String listName){
        super("List name " + listName + " is already taken");
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}
