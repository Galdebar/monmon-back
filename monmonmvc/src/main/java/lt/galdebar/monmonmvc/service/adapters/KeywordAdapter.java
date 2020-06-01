package lt.galdebar.monmonmvc.service.adapters;

import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingKeywordDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;


@Component
public class KeywordAdapter implements IsObjectAdapter<ShoppingKeywordDTO, KeywordDTO> {

    @Override
    public List<ShoppingKeywordDTO> bToA(List<KeywordDTO> externalList){
        List<ShoppingKeywordDTO> internalList = new ArrayList<>();
        for(KeywordDTO external:externalList){
            internalList.add(bToA(external));
        }

        return internalList;
    }

    @Override
    public ShoppingKeywordDTO bToA(KeywordDTO keywordDTO){
        return new ShoppingKeywordDTO(
                keywordDTO.getShoppingItemCategory(),
                keywordDTO.getKeyword()
        );
    }

    @Override
    public KeywordDTO aToB(ShoppingKeywordDTO internal){
        return new KeywordDTO(
                internal.getShoppingItemCategory(),
                internal.getKeyword()
        );
    }

    @Override
    public List<KeywordDTO> aToB(List<ShoppingKeywordDTO> shoppingKeywordDTOS) {
        List<KeywordDTO> externalList = new ArrayList<>();
        for(ShoppingKeywordDTO internal:shoppingKeywordDTOS){
            externalList.add(aToB(internal));
        }
        return externalList;
    }
}
