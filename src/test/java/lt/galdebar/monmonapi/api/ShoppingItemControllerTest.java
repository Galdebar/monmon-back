package lt.galdebar.monmonapi.api;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.persistence.repositories.ShoppingItemRepo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
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
    private MockMvc mockMvc;

    @Autowired
    private ShoppingItemsController itemsController;

    private static ObjectMapper objectMapper = new ObjectMapper();

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
        ShoppingItemDTO testItem = new ShoppingItemDTO(itemName);

        mockMvc.perform(post("/items/additem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNoAuthToken_whenUpdateItem_thenReturnForbidden() throws Exception {
        String itemName = "testItem";
        ShoppingItemDTO testItem = new ShoppingItemDTO(itemName);

        mockMvc.perform(post("/items/updateitem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNoAuthToken_whenDeleteItem_thenReturnForbidden() throws Exception {
        String itemName = "testItem";
        ShoppingItemDTO testItem = new ShoppingItemDTO(itemName);

        mockMvc.perform(post("/items/deleteitem")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andDo(print())
                .andExpect(status().isForbidden());
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
