package lt.galdebar.monmonapi.app.services.categories;

import lombok.RequiredArgsConstructor;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingCategoryEntity;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingKeywordDTO;
import lt.galdebar.monmonapi.categoriesparser.persistence.repositories.CategoriesRepo;
import lt.galdebar.monmonapi.categoriesparser.persistence.repositories.KeywordsRepo;
import lt.galdebar.monmonapi.categoriesparser.services.CategoriesSearchService;
import lt.galdebar.monmonapi.categoriesparser.services.ShoppingItemCategoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShoppingItemCategoryAndKeywordService {

    private final int MAX_RESULTS = 10;
    private final CategoriesRepo categoriesRepo;
    private final KeywordsRepo keywordsRepo;
    private final ShoppingItemCategoryService categoryService;
    private final CategoriesSearchService categoriesSearchService;


    @Transactional
    public List<CategoryDTO> getAllCategories() {
        return categoriesRepo
                .findAll()
                .stream()
                .map(ShoppingCategoryEntity::getDTO)
                .collect(Collectors.toList());
    }


    public List<ShoppingKeywordDTO> searchAutocomplete(ShoppingKeywordDTO shoppingKeywordDTO) {
        return categoriesSearchService.findKeywords(shoppingKeywordDTO, MAX_RESULTS);
    }
}
