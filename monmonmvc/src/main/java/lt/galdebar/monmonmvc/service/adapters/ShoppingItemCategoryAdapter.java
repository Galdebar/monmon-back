package lt.galdebar.monmonmvc.service.adapters;

import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingCategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingCategoryEntity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ShoppingItemCategoryAdapter implements IsObjectAdapter<ShoppingCategoryDTO, ShoppingCategoryEntity> {
    @Override
    public ShoppingCategoryDTO bToA(ShoppingCategoryEntity shoppingCategoryEntity) {
        Set<String> keywords = new HashSet<>();
        shoppingCategoryEntity.getKeywords().forEach(shoppingKeywordEntity -> keywords.add(shoppingKeywordEntity.getKeyword()));

        return new ShoppingCategoryDTO(shoppingCategoryEntity.getCategoryName(), keywords);
    }

    @Override
    public List<ShoppingCategoryDTO> bToA(List<ShoppingCategoryEntity> shoppingCategoryEntities) {
        List<ShoppingCategoryDTO> shoppingCategoryDTOList = new ArrayList<>();
        shoppingCategoryEntities.forEach(categoryDAO -> shoppingCategoryDTOList.add(bToA(categoryDAO)));
        return shoppingCategoryDTOList;
    }

    @Override
    public ShoppingCategoryEntity aToB(ShoppingCategoryDTO shoppingCategoryDTO) {
        return null;
    }

    @Override
    public List<ShoppingCategoryEntity> aToB(List<ShoppingCategoryDTO> shoppingCategoryDTOS) {
        return null;
    }
}
