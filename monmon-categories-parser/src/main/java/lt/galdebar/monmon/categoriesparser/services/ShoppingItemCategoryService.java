package lt.galdebar.monmon.categoriesparser.services;

import lt.galdebar.monmon.categoriesparser.persistence.repositories.CategoriesRepo;
import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingCategoryEntity;
import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingCategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingKeywordDTO;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.util.CharArraySet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Handles retrieval of shopping item categories and search.
 */
@Service
public class ShoppingItemCategoryService {
    /***
     * Limits the amount of search autocomplete results.
     */
    private final int MAX_RESULTS = 10;
    private final CategoryDTOToEntityConverter CATEGORY_DTO_ENTITY_ADAPTER = new CategoryDTOToEntityConverter();
    private Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);

    @Autowired
    private CategoriesSearchService searchService;

    @Autowired
    private CategoriesRepo categoryRepo;




    /**
     * Keyword search autocomplete.
     *
     * @param keywordDTO the keyword dto. Keyword field must not be null or empty.
     * @return list of keywords that are the closest match. List size limited by the MAX_RESULTS value.
     */
    public List<ShoppingKeywordDTO> searchKeywordAutocomplete(ShoppingKeywordDTO keywordDTO) {
        List<ShoppingKeywordDTO> foundKeywords = searchService.findKeywords(
                keywordDTO,
                MAX_RESULTS
        );
        List<ShoppingKeywordDTO> convertedKeywords = foundKeywords;
        return convertedKeywords;
    }

    /**
     * Find category by keyword. Returns one result. If nothing found. assign "Uncategorized"
     *
     * @param keywordDTO the keyword dto. Keyword field must not be null or empty.
     * @return closest matching category.
     */
    public ShoppingCategoryDTO findCategoryByKeyword(ShoppingKeywordDTO keywordDTO) {

        ShoppingCategoryDTO dtoToReturn;
        List<ShoppingCategoryDTO> foundKeywords = searchService.findCategoriesByKeyword(
                keywordDTO
        );
        if(foundKeywords.size() ==0 || foundKeywords.get(0).getKeywords().stream().noneMatch(keywordDTO.getKeyword()::equalsIgnoreCase)){
            dtoToReturn = searchService.getUncategorized();
        } else {
            dtoToReturn = foundKeywords.get(0);

        }
        return dtoToReturn;

    }

    /**
     * Gets all categories.
     *
     * @return list of all categories.
     */
    public List<ShoppingCategoryDTO> getAllCategories() {
        List<ShoppingCategoryEntity> categories = categoryRepo.findAll();
        return CATEGORY_DTO_ENTITY_ADAPTER.convertEntitiesToDTOs(categories);
    }

    /**
     * Search shopping category.
     *
     * @param itemCategory the item category
     * @return list of matching shopping categories. Size limited by the MAX_RESULTS value.
     */
    public ShoppingCategoryDTO searchCategory(ShoppingCategoryDTO itemCategory) {
        ShoppingCategoryDTO foundCategory = searchService.searchCategory(
                itemCategory
        );
        return foundCategory;
    }

}
