package lt.galdebar.monmonmvc.service.adapters;

import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class KeywordAdapter {

    public List<ShoppingKeywordDTO> externalToInternalList(List<KeywordDTO> externalList){
        List<ShoppingKeywordDTO> internalList = new ArrayList<>();
        for(KeywordDTO external:externalList){
            internalList.add(externalToInternal(external));
        }

        return internalList;
    }

    public ShoppingKeywordDTO externalToInternal(KeywordDTO keywordDTO){
        return new ShoppingKeywordDTO(
                keywordDTO.getShoppingItemCategory(),
                keywordDTO.getKeyword()
        );
    }

    public KeywordDTO internalToExternal(ShoppingKeywordDTO internal){
        return new KeywordDTO(
                internal.getShoppingItemCategory(),
                internal.getKeyword()
        );
    }
}
