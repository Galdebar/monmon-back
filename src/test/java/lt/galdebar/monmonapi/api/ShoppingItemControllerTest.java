package lt.galdebar.monmonapi.api;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemEntity;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonapi.persistence.repositories.ShoppingListRepo;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@TestPropertySource(locations = "classpath:test.properties")
public class ShoppingItemControllerTest {

    @Autowired
    private ShoppingItemRepo itemRepo;

    @Autowired
    private ShoppingListRepo listRepo;

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShoppingItemsController itemsController;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    public void setup() {
        itemRepo.deleteAll();
        listRepo.deleteAll();
    }

    @Test
    void contextLoads() {
        assertNotNull(itemsController);
    }

    @Test
    void givenNoAuthToken_whenGetAllItems_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/items/getall")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNoAuthToken_whenAddItem_thenReturnForbidden() throws Exception {
        String itemName = "testItem";
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(itemName);

        mockMvc.perform(post("/items/additem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNoAuthToken_whenUpdateItem_thenReturnForbidden() throws Exception {
        String itemName = "testItem";
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(itemName);

        mockMvc.perform(post("/items/updateitem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNoAuthToken_whenDeleteItem_thenReturnForbidden() throws Exception {
        String itemName = "testItem";
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(itemName);

        mockMvc.perform(post("/items/deleteitem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenValidToken_whenGetAll_thenReturnArrayOfItems() throws Exception {
        String testListName = "testList";
        String testListPassword = "testListPassword";
        AuthTokenDTO authTokenDTO = createListAndLogin(testListName, testListPassword);

        String testItemName1 = "item1";
        String testItemName2 = "item2";
        ShoppingListEntity listEntity = listRepo.findByNameIgnoreCase(testListName);

        ShoppingItemEntity testItem1 = new ShoppingItemEntity();
        testItem1.setComment("");
        testItem1.setInCart(false);
        testItem1.setQuantity(1);
        testItem1.setItemCategory("");
        testItem1.setItemName(testItemName1);
        testItem1.setShoppingList(listEntity);
        ShoppingItemEntity testItem2 = new ShoppingItemEntity();
        testItem2.setComment("");
        testItem2.setInCart(false);
        testItem2.setQuantity(1);
        testItem2.setItemCategory("");
        testItem2.setItemName(testItemName2);
        testItem2.setShoppingList(listEntity);

        itemRepo.save(testItem1);
        itemRepo.save(testItem2);
        listRepo.save(listEntity);


        String response = mockMvc.perform(get("/items/getall")
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ShoppingItemDTO> actualItems = objectMapper.readValue(response, new TypeReference<List<ShoppingItemEntity>>(){});
        assertNotNull(response);
        assertEquals(2,actualItems.size());

    }


    @Test
        // dunnoo wtf is going on with this-- always returns Bad Request
    void givenValidItem_whenAddItem_thenItemAddedToDBandReturnSameItem() throws Exception {
        String testListName = "testList";
        String testListPassword = "testListPassword";
        AuthTokenDTO authTokenDTO = createListAndLogin(testListName, testListPassword);

        String testItemName = "testItem";
        ShoppingItemDTO expectedItem = new ShoppingItemDTO();
        expectedItem.setItemName(testItemName);
        Map<String, String> postRequest = new HashMap<>();
        postRequest.put("itemName", testItemName);
        postRequest.put("itemCategory", "");
        postRequest.put("quantity","1");
        postRequest.put("comment","");

        String response = mockMvc.perform(post("/items/additem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(expectedItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ShoppingItemDTO actualItem = objectMapper.readValue(response, ShoppingItemDTO.class);
        ShoppingListEntity actualList = listRepo.findByNameIgnoreCase(testItemName);

        assertNotNull(actualItem);
        assertTrue(itemRepo.count() != 0);
        assertEquals(expectedItem.getItemName(), actualItem.getItemName());
        assertNotNull(actualItem.getId());
        assertNotNull(itemRepo.findById(actualItem.getId()));


    }

    private AuthTokenDTO createListAndLogin(String listName, String listPassword) throws Exception {
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

        return objectMapper.readValue(response, AuthTokenDTO.class);
    }

    //unauthorized with CRUD methods without token.
    // test CRUD methods with auth token.
    // get all items
    // delete all items
    // unmark all items
    // bad request if adding blank item name.
    // bad request if quantity less than 1

    // later connect categories module and test category
}
