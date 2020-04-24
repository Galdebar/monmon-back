package lt.galdebar.monmonmvc.service.adapters;

import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CategoryAdapter implements IsObjectAdapter<ShoppingCategoryDTO, CategoryDTO> {

    @Override
    public List<ShoppingCategoryDTO> bToA(List<CategoryDTO> externalList) {
        List<ShoppingCategoryDTO> dtoList = new ArrayList<>();
        for (CategoryDTO external : externalList) {
            dtoList.add(bToA(external));
        }
        return dtoList;
    }

    @Override
    public ShoppingCategoryDTO bToA(CategoryDTO categoryDTO) {
        return new ShoppingCategoryDTO(
                categoryDTO.getCategoryName(),
                categoryDTO.getKeywords()
        );
    }

    @Override
    public CategoryDTO aToB(ShoppingCategoryDTO internal){
        return new CategoryDTO(
                internal.getCategoryName(),
                internal.getKeywords()
        );
    }

    @Override
    public List<CategoryDTO> aToB(List<ShoppingCategoryDTO> shoppingCategoryDTOS) {
        List<CategoryDTO> externalList = new ArrayList<>();
        for(ShoppingCategoryDTO internal:shoppingCategoryDTOS){
            externalList.add(aToB(internal));
        }
        return externalList;
    }
}
