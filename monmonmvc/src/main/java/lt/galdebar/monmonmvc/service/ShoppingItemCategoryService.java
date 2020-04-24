package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDTO;
import lt.galdebar.monmon.categoriesparser.services.CategoriesSearchService;
import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingCategoryEntity;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.persistence.repositories.ItemCategoryRepo;
import lt.galdebar.monmonmvc.service.adapters.CategoryAdapter;
import lt.galdebar.monmonmvc.service.adapters.KeywordAdapter;
import lt.galdebar.monmonmvc.service.adapters.ShoppingItemCategoryAdapter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Handles retrieval of shopping item categories and search.
 */
@Service
public class ShoppingItemCategoryService {
    /***
     * Limits the amount of search autocomplete results.
     */
    private final int MAX_RESULTS = 10;
    private final ShoppingItemCategoryAdapter CATEGORY_DTO_ENTITY_ADAPTER = new ShoppingItemCategoryAdapter();

    @Autowired
    private CategoriesSearchService searchService;

    @Autowired
    private ItemCategoryRepo categoryRepo;

    @Autowired
    private CategoryAdapter externalCategoryAdapter;

    @Autowired
    private KeywordAdapter externalKeywordAdapter;

    private Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);


    /**
     * Keyword search autocomplete.
     *
     * @param keywordDTO the keyword dto. Keyword field must not be null or empty.
     * @return list of keywords that are the closest match. List size limited by the MAX_RESULTS value.
     */
    public List<ShoppingKeywordDTO> searchKeywordAutocomplete(ShoppingKeywordDTO keywordDTO) {
        List<KeywordDTO> foundKeywords = searchService.findKeywords(
                externalKeywordAdapter.aToB(keywordDTO),
                MAX_RESULTS
        );
        List<ShoppingKeywordDTO> convertedKeywords = externalKeywordAdapter.bToA(foundKeywords);
        return convertedKeywords;
    }

    /**
     * Find category by keyword. Returns one result. If nothing found. assign "Uncategorized"
     *
     * @param keywordDTO the keyword dto. Keyword field must not be null or empty.
     * @return closest matching category.
     */
    ShoppingCategoryDTO findCategoryByKeyword(ShoppingKeywordDTO keywordDTO) {

        ShoppingCategoryDTO dtoToReturn;
        List<CategoryDTO> foundKeywords = searchService.findCategoriesByKeyword(
                externalKeywordAdapter.aToB(keywordDTO)
        );
        if(foundKeywords.size() ==0 || foundKeywords.get(0).getKeywords().stream().noneMatch(keywordDTO.getKeyword()::equalsIgnoreCase)){
            dtoToReturn = externalCategoryAdapter.bToA(searchService.getUncategorized());
        } else {
            dtoToReturn = externalCategoryAdapter.bToA(foundKeywords.get(0));

        }
        return dtoToReturn;

    }

    /**
     * Gets all categories.
     *
     * @return list of all categories.
     */
    public List<ShoppingCategoryDTO> getAllCategories() {
        List<ShoppingCategoryEntity> categories = (List<ShoppingCategoryEntity>) categoryRepo.findAll();
        return CATEGORY_DTO_ENTITY_ADAPTER.bToA(categories);
    }

    /**
     * Search shopping category.
     *
     * @param itemCategory the item category
     * @return list of matching shopping categories. Size limited by the MAX_RESULTS value.
     */
    ShoppingCategoryDTO searchCategory(ShoppingCategoryDTO itemCategory) {
        CategoryDTO foundCategory = searchService.searchCategory(
                externalCategoryAdapter.aToB(itemCategory)
        );
        return externalCategoryAdapter.bToA(foundCategory);
    }

}
