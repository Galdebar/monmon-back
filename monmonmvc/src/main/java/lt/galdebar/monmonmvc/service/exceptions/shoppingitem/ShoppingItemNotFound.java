package lt.galdebar.monmonmvc.service.exceptions.shoppingitem;

public class ShoppingItemNotFound extends ShoppingItemServiceException {
    private static final String MESSAGE= "Shopping item not found. ";
    private final String itemId;

    public ShoppingItemNotFound( String itemId) {
        super(MESSAGE);
        this.itemId = itemId;
    }

    @Override
    public String getMessage() {
        if(itemId != null){
            String mainMessage = super.getMessage();
            String details = String.format("Item id: %s. ", itemId);
            return mainMessage + details;
        }else return super.getMessage();
    }
}
