package lt.galdebar.monmonapi.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.galdebar.monmonapi.ListTestCoontainersConfig;
import lt.galdebar.monmonapi.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.persistence.repositories.ShoppingListRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import javax.print.attribute.standard.Media;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {ListTestCoontainersConfig.Initializer.class})
@TestPropertySource(locations = "classpath:test.properties")
class ShoppingListControllerTest {

    @Autowired
    private ShoppingListController controller;

    @Autowired
    private ShoppingListRepo repo;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void contextLoads() {
        assertNotNull(controller);
    }

    @BeforeEach
    void beforeEach() {
        repo.deleteAll();
    }

    @Test
    void givenValidFields_whenCreateList_thenListAddedToDB() throws Exception {
        String listName = "testList";
        String listPassword = "testPass";
        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("name", listName);
        requestObject.put("password", listPassword);

        assertEquals(0, repo.count());

        String response = mockMvc.perform(post("/lists/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestObject))
        )
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<ShoppingListEntity> savedLists = (List<ShoppingListEntity>) repo.findAll();

        assertNotNull(response);
        assertFalse(response.trim().isEmpty());

        assertEquals(1, savedLists.size());

        ShoppingListEntity savedList = savedLists.get(0);

        assertNotNull(savedList);
        assertTrue(passwordEncoder.matches(listPassword, savedList.getPassword()));

    }

    @Test
    void givenValidCredentials_whenLogin_thenReturnAuthToken() throws Exception {
        String listName = "testList";
        String listPassword = "testPass";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("name", listName);
        requestObject.put("password", listPassword);

        ShoppingListEntity entityToSave = new ShoppingListEntity();
        entityToSave.setName(listName);
        entityToSave.setPassword(passwordEncoder.encode(listPassword));
        entityToSave.setTimeCreated(LocalDateTime.now());
        entityToSave.setLastUsedTime(LocalDateTime.now());
        repo.save(entityToSave);

        String response = mockMvc.perform(post("/lists/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestObject)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(response);

        AuthTokenDTO responseObject = objectMapper.readValue(response, AuthTokenDTO.class);

        assertNotNull(responseObject);
        assertTrue(listName.equals(responseObject.getName()));
    }

    //bad request if create list that already exists
    //bad request if password is empty
    //maybe add two passwords ?
    //not found if login with incorrect name
    //unahtorized if invalid password.

}