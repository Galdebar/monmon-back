package lt.galdebar.monmonmvc.service;

import lt.galdebar.monmonmvc.persistence.domain.dao.ShoppingItemDAO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingItemDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingKeywordDTO;
import lt.galdebar.monmonmvc.persistence.repositories.MongoDBRepo;
//import org.junit.Test;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;


@SpringJUnitConfig
public class ShoppingItemServiceTest {
    @Configuration
    public static class TestConfig {

        @Bean
        public ShoppingItemService service() {
            return new ShoppingItemService();
        }
    }

    @Autowired
    private ShoppingItemService service;

    @MockBean
    private MongoDBRepo mongoDBRepo;


    @MockBean
    private ShoppingItemCategoryService shoppingItemCategoryService;

    @MockBean
    private TestEntityManager entityManager;


    @Test
    void addItem() {
        ShoppingItemDTO testShoppingItem = new ShoppingItemDTO(
                "1",
                "TestItem",
                "TestCategory",
                1,
                "TestComment",
                false
        );

        ShoppingCategoryDTO mockCategoryDTO = new ShoppingCategoryDTO(
                "FoundCategory",
                new HashSet<>()
        );
        mockCategoryDTO.getKeywords().add(testShoppingItem.getItemName());

        ShoppingKeywordDTO keyword = new ShoppingKeywordDTO("", testShoppingItem.getItemName());
        Mockito.when(shoppingItemCategoryService.findCategoryByKeyword(any(ShoppingKeywordDTO.class)))
                .thenReturn(mockCategoryDTO);
        Mockito.when(mongoDBRepo.insert(any(ShoppingItemDAO.class))).thenReturn(
                new ShoppingItemDAO()
        );

        ShoppingItemDTO receivedItem = service.addItem(testShoppingItem);

        assertNotNull(receivedItem);
    }

    @Test
    void getItemById() {
    }

    @Test
    void getItemsByCategory() {
    }

    @Test
    void getAll() {
    }

    @Test
    void updateItem() {
    }

    @Test
    void updateItems() {
    }

    @Test
    void deleteItem() {
    }

    @Test
    void deleteItems() {
    }
}