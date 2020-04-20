package lt.galdebar.monmonmvc.service.adapters;

import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CategoryAdapter {

    public List<ShoppingCategoryDTO> externalToInternalList(List<CategoryDTO> externalList) {
        List<ShoppingCategoryDTO> dtoList = new ArrayList<>();
        for (CategoryDTO external : externalList) {
            dtoList.add(externalToInternal(external));
        }
        return dtoList;
    }

    public ShoppingCategoryDTO externalToInternal(CategoryDTO categoryDTO) {
        return new ShoppingCategoryDTO(
                categoryDTO.getCategoryName(),
                categoryDTO.getKeywords()
        );
    }

    public CategoryDTO internalToExternal(ShoppingCategoryDTO internal){
        return new CategoryDTO(
                internal.getCategoryName(),
                internal.getKeywords()
        );
    }
}
