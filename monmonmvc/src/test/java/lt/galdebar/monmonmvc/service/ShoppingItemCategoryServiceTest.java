package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import javax.persistence.EntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ShoppingItemCategoryServiceTest {
    @Autowired
    private ShoppingItemCategoryService shoppingItemCategoryService;

    @MockBean
    private EntityManager entityManager;


    @Test
    void searchKeywordAutocomplete() {
        ShoppingKeywordDTO testKeyword = new ShoppingKeywordDTO("Category", "Keyword");
        List<ShoppingKeywordDTO> actualList = shoppingItemCategoryService.searchKeywordAutocomplete(testKeyword);

        assertNotNull(actualList);

    }

    @Test
    void findCategoryByKeyword() {
    }

    @Test
    void getAllCategories() {
    }
}