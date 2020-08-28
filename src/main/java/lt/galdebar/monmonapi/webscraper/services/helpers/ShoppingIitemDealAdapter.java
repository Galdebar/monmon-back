package lt.galdebar.monmonapi.webscraper.services.helpers;

import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;

import java.util.ArrayList;
import java.util.List;

public class ShoppingIitemDealAdapter {
    public ShoppingItemDealDTO entityToDTO(ShoppingItemDealEntity entity) {
        return new ShoppingItemDealDTO(
                entity.getOriginalTitle(),
                entity.getItemKeyword(),
                entity.getItemBrand(),
                entity.getShopTitle(),
                entity.getPrice()
        );
    }

    public List<ShoppingItemDealDTO> entityToDTO(List<ShoppingItemDealEntity> entities) {
        List<ShoppingItemDealDTO> dtos = new ArrayList<>();
        for (ShoppingItemDealEntity entity : entities) {
            dtos.add(entityToDTO(entity));
        }

        return dtos;
    }

    public ShoppingItemDealEntity dtoToEntity(ShoppingItemDealDTO dto) {
        ShoppingItemDealEntity entity = new ShoppingItemDealEntity();
        entity.setOriginalTitle(dto.getOriginalTitle());
        entity.setItemKeyword(dto.getItemKeyword());
        entity.setItemBrand(dto.getItemBrand());
        entity.setShopTitle(dto.getShopTitle());
        entity.setPrice(dto.getPrice());

        return entity;
    }

    public List<ShoppingItemDealEntity> dtoToEntity(List<ShoppingItemDealDTO> dtos) {
        List<ShoppingItemDealEntity> entities = new ArrayList<>();
        for (ShoppingItemDealDTO dto : dtos) {
            entities.add(dtoToEntity(dto));
        }

        return entities;
    }
}
