package lt.galdebar.monmonmvc.service.adapters;

import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingItemEntity;
import lt.galdebar.monmonscraper.persistence.domain.ShoppingItemDealDTO;

import java.util.ArrayList;
import java.util.List;

public class ShoppingItemAdapter implements IsObjectAdapter<ShoppingItemDTO, ShoppingItemEntity> {
    @Override
    public ShoppingItemDTO bToA(ShoppingItemEntity shoppingItemEntity) {
        return new ShoppingItemDTO(
                shoppingItemEntity.id,
                shoppingItemEntity.itemName,
                shoppingItemEntity.itemCategory,
                shoppingItemEntity.quantity,
                shoppingItemEntity.comment,
                shoppingItemEntity.isInCart,
                shoppingItemEntity.users,
                new ShoppingItemDealDTO()
        );
    }

    @Override
    public List<ShoppingItemDTO> bToA(List<ShoppingItemEntity> shoppingItemEntities) {
        List<ShoppingItemDTO> shoppingItemDTOList = new ArrayList<>();
        shoppingItemEntities.forEach(dao -> shoppingItemDTOList.add(bToA(dao)));
        return shoppingItemDTOList;
    }

    @Override
    public ShoppingItemEntity aToB(ShoppingItemDTO shoppingItemDTO) {
        ShoppingItemEntity shoppingItemEntity = new ShoppingItemEntity();
        shoppingItemEntity.id = shoppingItemDTO.getId();
        shoppingItemEntity.itemName = shoppingItemDTO.getItemName();
        shoppingItemEntity.itemCategory = shoppingItemDTO.getItemCategory();
        shoppingItemEntity.quantity = shoppingItemDTO.getQuantity();
        shoppingItemEntity.comment = shoppingItemDTO.getComment();
        shoppingItemEntity.isInCart = shoppingItemDTO.isInCart();
        if(shoppingItemDTO.getUsers() != null){
            shoppingItemEntity.users.addAll(shoppingItemDTO.getUsers());
        }
        return shoppingItemEntity;
    }

    @Override
    public List<ShoppingItemEntity> aToB(List<ShoppingItemDTO> shoppingItemDTOS) {
        List<ShoppingItemEntity> shoppingItemEntityList = new ArrayList<>();
        shoppingItemDTOS.forEach(dto -> shoppingItemEntityList.add(aToB(dto)));
        return shoppingItemEntityList;
    }
}
