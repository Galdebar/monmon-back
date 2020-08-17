package lt.galdebar.monmonapi.app.persistence.domain.shoppingitems;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.app.services.shoppingitems.exceptions.InvalidShoppingItemRequest;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;

import javax.persistence.*;

@Data
@RequiredArgsConstructor
@Entity(name = "shopping_item")
@Table(name = "shopping_items")
public class ShoppingItemEntity {
    @Id
    @GeneratedValue
    private Long id;
    private String itemName;
    private String itemCategory = "";
    private Integer quantity = 1;
    private String comment = "";
    private boolean isInCart = false;
    @ManyToOne
    @JoinColumn(name = "list_id")
    private ShoppingListEntity shoppingList;


    public ShoppingItemEntity(ShoppingItemDTO dto) {
        dto.checkIfValid();
        this.itemName = dto.getItemName();
        this.isInCart = dto.isInCart();

        if (dto.getId() != null) {
            this.id = dto.getId();
        }

        if (dto.getItemCategory() != null) {
            this.itemCategory = dto.getItemCategory();
        }

        if(dto.getQuantity() != null){
            this.quantity = dto.getQuantity();
        }

        if (dto.getComment() != null) {
            this.comment = dto.getComment();
        }
    }

    public ShoppingItemDTO getDTO() {
        ShoppingItemDTO dto = new ShoppingItemDTO();
        dto.setId(id);
        dto.setItemName(itemName);
        dto.setItemCategory(itemCategory);
        dto.setQuantity(quantity);
        dto.setComment(comment);
        dto.setInCart(isInCart);
        return dto;
    }

    private void checkIfDTOValid(ShoppingItemDTO shoppingItemDTO) {
        if (shoppingItemDTO.getItemName() == null || shoppingItemDTO.getItemName().trim().isEmpty()) {
            throw new InvalidShoppingItemRequest("Item must have a name");
        }
        if (shoppingItemDTO.getQuantity() != null && shoppingItemDTO.getQuantity() < 1) {
            throw new InvalidShoppingItemRequest("Item quantity must be greater than 1");
        }
    }
}
