package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmon.categoriesparser.persistence.domain.KeywordDTO;
import lt.galdebar.monmon.categoriesparser.persistence.repositories.CategoriesRepo;
import lt.galdebar.monmon.categoriesparser.services.CategoriesSearchService;
import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingCategoryEntity;
import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingKeywordEntity;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.persistence.repositories.ItemCategoryRepo;
import lt.galdebar.monmonmvc.service.adapters.CategoryAdapter;
import lt.galdebar.monmonmvc.service.adapters.KeywordAdapter;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import org.apache.lucene.search.Query;
import org.hibernate.search.jpa.FullTextEntityManager;
import org.hibernate.search.jpa.Search;
import org.hibernate.search.query.dsl.QueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.StringReader;
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

    @Autowired
    private CategoriesSearchService searchService;

    @Autowired
    private ItemCategoryRepo categoryRepo;

    @Autowired
    private CategoryAdapter categoryAdapter;

    @Autowired
    private KeywordAdapter keywordAdapter;

    private Analyzer analyzer = new StandardAnalyzer(CharArraySet.EMPTY_SET);


    /**
     * Keyword search autocomplete.
     *
     * @param keywordDTO the keyword dto. Keyword field must not be null or empty.
     * @return list of keywords that are the closest match. List size limited by the MAX_RESULTS value.
     */
    public List<ShoppingKeywordDTO> searchKeywordAutocomplete(ShoppingKeywordDTO keywordDTO) {
        List<KeywordDTO> foundKeywords = searchService.findKeywords(
                keywordAdapter.internalToExternal(keywordDTO),
                MAX_RESULTS
        );
        List<ShoppingKeywordDTO> convertedKeywords = keywordAdapter.externalToInternalList(foundKeywords);
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
                keywordAdapter.internalToExternal(keywordDTO)
        );
        if(foundKeywords.size() ==0 || foundKeywords.get(0).getKeywords().stream().noneMatch(keywordDTO.getKeyword()::equalsIgnoreCase)){
            dtoToReturn = categoryAdapter.externalToInternal(searchService.getUncategorized());
        } else {
            dtoToReturn = categoryAdapter.externalToInternal(foundKeywords.get(0));

        }
        return dtoToReturn;

    }

    /**
     * Gets all categories.
     *
     * @return list of all categories.
     */
//    @Transactional
    public List<ShoppingCategoryDTO> getAllCategories() {
        List<ShoppingCategoryEntity> categories = (List<ShoppingCategoryEntity>) categoryRepo.findAll();
        return categoryEntitiesToDTOS(categories);
    }

    /**
     * Search shopping category.
     *
     * @param itemCategory the item category
     * @return list of matching shopping categories. Size limited by the MAX_RESULTS value.
     */
    ShoppingCategoryDTO searchCategory(ShoppingCategoryDTO itemCategory) {
        CategoryDTO foundCategory = searchService.searchCategory(
                categoryAdapter.internalToExternal(itemCategory)
        );
        return categoryAdapter.externalToInternal(foundCategory);
    }

    private ShoppingCategoryDTO categoryEntityToDTO(ShoppingCategoryEntity shoppingCategoryEntity) {
        Set<String> keywords = new HashSet<>();
        shoppingCategoryEntity.getKeywords().forEach(shoppingKeywordEntity -> keywords.add(shoppingKeywordEntity.getKeyword()));

        return new ShoppingCategoryDTO(shoppingCategoryEntity.getCategoryName(), keywords);
    }

    private List<ShoppingCategoryDTO> categoryEntitiesToDTOS(List<ShoppingCategoryEntity> shoppingCategoryEntityList) {
        List<ShoppingCategoryDTO> shoppingCategoryDTOList = new ArrayList<>();
        shoppingCategoryEntityList.forEach(categoryDAO -> shoppingCategoryDTOList.add(categoryEntityToDTO(categoryDAO)));
        return shoppingCategoryDTOList;
    }

}
