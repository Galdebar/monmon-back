package lt.galdebar.monmonapi.app.api;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.app.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.app.persistence.repositories.ShoppingListRepo;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.CategoryDTO;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingCategoryEntity;
import lt.galdebar.monmonapi.categoriesparser.persistence.domain.ShoppingKeywordDTO;
import lt.galdebar.monmonapi.categoriesparser.persistence.repositories.CategoriesRepo;
import lt.galdebar.monmonapi.categoriesparser.persistence.repositories.KeywordsRepo;
import lt.galdebar.monmonapi.webscraper.scheduledtasks.RunScraper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@TestPropertySource(locations = "classpath:test.properties")
@ComponentScan(basePackages = {"lt.galdebaar"}, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RunScraper.class)
})
class ShoppingItemCategoryAndKeywordControllerTest {

    @Autowired
    private ShoppingListRepo listRepo;

    @Autowired
    private CategoriesRepo categoriesRepo;

    @Autowired
    private KeywordsRepo keywordsRepo;

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private ShoppingItemCategoryAndKeywordController controller;

    @BeforeEach
    void beforeEach() {
        listRepo.deleteAll();
    }

    @Test
    void contextLoads() {
        assertNotNull(controller);
    }


    @Test
    void givenValidToken_whenGetAllCategories_thenReturnListOfCategories() throws Exception {
        String authToken = createListAndLoginAndGetAuthToken();

        String response = mockMvc.perform(get("/categories/getall")
                .header("Authorization", "Bearer " + authToken))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<CategoryDTO> expectedCategories = categoriesRepo.findAll()
                .stream()
                .map(ShoppingCategoryEntity::getDTO)
                .collect(Collectors.toList());

        List<CategoryDTO> actualCategories = objectMapper.readValue(response, new TypeReference<List<CategoryDTO>>() {
        });

        assertEquals(expectedCategories.size(), actualCategories.size());
        assertEquals(
                expectedCategories
                        .stream()
                        .map(CategoryDTO::getCategoryName)
                        .collect(Collectors.toList()),
                actualCategories
                        .stream()
                        .map(CategoryDTO::getCategoryName)
                        .collect(Collectors.toList())
        );


    }

    @Test
    void givenInvalidToken_whenGetAllCategories_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/categories/getall")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/categories/getall")
                .header("Authorization", "Bearer " + "ioauhwdihawd"))
                .andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/categories/getall")
                .header("Authorization", "Bearer " + ""))
                .andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/categories/getall")
                .header("Authorization", "Bearer " + "    "))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenValidToken_whenSearch_thenReturnListOfValidKeywords() throws Exception {
        String authToken = createListAndLoginAndGetAuthToken();
        String expectedCategory = "Beverages";

        String searchKeyword1 = "b";
        String searchKeyword2 = "be";
        String searchKeyword3 = "bev";
        String searchKeyword4 = "beve";
        String searchKeyword5 = "bever";
        String searchKeyword6 = "bever";
        String searchKeyword7 = "bevera";
        String searchKeyword8 = "beverag";

        String searchResponse1 = createAndPostSearchRequest(searchKeyword1, authToken);
        String searchResponse2 = createAndPostSearchRequest(searchKeyword2, authToken);
        String searchResponse3 = createAndPostSearchRequest(searchKeyword3, authToken);
        String searchResponse4 = createAndPostSearchRequest(searchKeyword4, authToken);
        String searchResponse5 = createAndPostSearchRequest(searchKeyword5, authToken);
        String searchResponse6 = createAndPostSearchRequest(searchKeyword6, authToken);
        String searchResponse7 = createAndPostSearchRequest(searchKeyword7, authToken);
        String searchResponse8 = createAndPostSearchRequest(searchKeyword8, authToken);

        assertEquals("[]", searchResponse1); //because of stopwords
        assertEquals("[]", searchResponse2); // because of stopwords;
        assertTrue(searchResponse3.contains(expectedCategory));
        assertTrue(searchResponse4.contains(expectedCategory));
        assertTrue(searchResponse5.contains(expectedCategory));
        assertTrue(searchResponse6.contains(expectedCategory));
        assertTrue(searchResponse7.contains(expectedCategory));
        assertTrue(searchResponse8.contains(expectedCategory));
    }

    @Test
    void givenInvalidToken_whenSearch_thenReturnForbidden() throws Exception {
        String keyword = "beverage";
        ShoppingKeywordDTO searchKeyword = new ShoppingKeywordDTO("", keyword);

        mockMvc.perform(post("/categories/search")
                .content(objectMapper.writeValueAsString(searchKeyword))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/categories/search")
                .header("Authorization", "Bearer " + "ioauhwdihawd")
                .content(objectMapper.writeValueAsString(searchKeyword))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/categories/search")
                .header("Authorization", "Bearer " + "")
                .content(objectMapper.writeValueAsString(searchKeyword))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/categories/search")
                .header("Authorization", "Bearer " + "    ")
                .content(objectMapper.writeValueAsString(searchKeyword))
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    //get all categories

    //find category by keyword ?

    //search autocomplete

    //unauthorized if invalid or blank tokens


    private String createListAndLoginAndGetAuthToken() throws Exception {
        String listName = "testList";
        String listPassword = "testListPassword";

        Map<String, String> listRequest = new HashMap<>();
        listRequest.put("name", listName);
        listRequest.put("password", listPassword);

        mockMvc.perform(post("/lists/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listRequest)))
                .andExpect(status().isOk());

        String response = mockMvc.perform(post("/lists/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        if (response == null) {
            throw new Exception("response null");
        }

        AuthTokenDTO authToken = objectMapper.readValue(response, AuthTokenDTO.class);

        return authToken.getToken();
    }

    private String createAndPostSearchRequest(String keyword, String authToken) throws Exception {
        ShoppingKeywordDTO searchKeyword = new ShoppingKeywordDTO("", keyword);
        return mockMvc.perform(post("/categories/search")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(searchKeyword))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }


}