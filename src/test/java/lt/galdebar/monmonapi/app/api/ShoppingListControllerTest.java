package lt.galdebar.monmonapi.app.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.app.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.app.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonapi.app.persistence.repositories.ShoppingListRepo;
import lt.galdebar.monmonapi.app.services.shoppinglists.ShoppingListService;
import lt.galdebar.monmonapi.app.services.shoppinglists.exceptions.ListAlreadyExists;
import org.assertj.core.data.TemporalUnitOffset;
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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@TestPropertySource(locations = "classpath:test.properties")
class ShoppingListControllerTest {

    @Autowired
    private ShoppingListController controller;

    @Autowired
    private ShoppingListService service;

    @Autowired
    private ShoppingListRepo listRepo;

    @Autowired
    private ShoppingItemRepo itemRepo;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void contextLoads() {
        assertNotNull(controller);
        assertNotNull(service);
    }

    @BeforeEach
    void beforeEach() {
        listRepo.deleteAll();
    }

    @Test
    void givenValidFields_whenCreateList_thenListAddedToDB() throws Exception {
        String listName = "testList";
        String listPassword = "testPass";
        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("name", listName);
        requestObject.put("password", listPassword);

        assertEquals(0, listRepo.count());

        String response = mockMvc.perform(post("/lists/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestObject))
        )
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        List<ShoppingListEntity> savedLists = (List<ShoppingListEntity>) listRepo.findAll();

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
        listRepo.save(entityToSave);

        String response = mockMvc.perform(post("/lists/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestObject)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(response);

        AuthTokenDTO responseObject = objectMapper.readValue(response, AuthTokenDTO.class);

        assertNotNull(responseObject);
        assertEquals(listName, responseObject.getName());
        assertFalse(responseObject.getToken().trim().isEmpty());

    }

    @Test
    void givenInvalidPassword_whenLogin_thenReturnUnauthorized() throws Exception {
        String listName = "testList";
        String listPassword = "testPass";
        String invalidPassword = "opiupahwdlkn";

        Map<String, String> loginRequestObject = new HashMap<>();
        loginRequestObject.put("name", listName);
        loginRequestObject.put("password", invalidPassword);

        ShoppingListEntity entityToSave = new ShoppingListEntity();
        entityToSave.setName(listName);
        entityToSave.setPassword(passwordEncoder.encode(listPassword));
        entityToSave.setTimeCreated(LocalDateTime.now());
        entityToSave.setLastUsedTime(LocalDateTime.now());
        listRepo.save(entityToSave);

        String response = mockMvc.perform(post("/lists/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestObject)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getErrorMessage();

        assertNotNull(response);
        assertTrue(response.toLowerCase().contains("invalid password"));
    }

    @Test
    void givenInvalidListName_whenLogin_thenReturnNotFound() throws Exception {
        String listName = "testList";
        String listPassword = "testPass";
        String invalidListName = "opiupahwdlkn";

        Map<String, String> loginRequestObject = new HashMap<>();
        loginRequestObject.put("name", invalidListName);
        loginRequestObject.put("password", listPassword);

        ShoppingListEntity entityToSave = new ShoppingListEntity();
        entityToSave.setName(listName);
        entityToSave.setPassword(passwordEncoder.encode(listPassword));
        entityToSave.setTimeCreated(LocalDateTime.now());
        entityToSave.setLastUsedTime(LocalDateTime.now());
        listRepo.save(entityToSave);

        String response = mockMvc.perform(post("/lists/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestObject)))
                .andExpect(status().isNotFound())
                .andReturn().getResponse().getErrorMessage();

        assertNotNull(response);
        assertTrue(response.contains("not found"));
    }

    @Test
    void givenEmptyListName_whenLogin_thenReturnBadRequest() throws Exception {
        String listName = "testList";
        String listPassword = "testPass";
        String invalidListName = "";

        Map<String, String> loginRequestObject = new HashMap<>();
        loginRequestObject.put("name", invalidListName);
        loginRequestObject.put("password", listPassword);

        ShoppingListEntity entityToSave = new ShoppingListEntity();
        entityToSave.setName(listName);
        entityToSave.setPassword(passwordEncoder.encode(listPassword));
        entityToSave.setTimeCreated(LocalDateTime.now());
        entityToSave.setLastUsedTime(LocalDateTime.now());
        listRepo.save(entityToSave);

        String response = mockMvc.perform(post("/lists/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestObject)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getErrorMessage();

        assertNotNull(response);
        assertTrue(response.contains("empty"));
    }

    @Test
    void givenBlankListName_whenLogin_thenReturnBadRequest() throws Exception {
        String listName = "testList";
        String listPassword = "testPass";
        String invalidListName = "     ";

        Map<String, String> loginRequestObject = new HashMap<>();
        loginRequestObject.put("name", invalidListName);
        loginRequestObject.put("password", listPassword);

        ShoppingListEntity entityToSave = new ShoppingListEntity();
        entityToSave.setName(listName);
        entityToSave.setPassword(passwordEncoder.encode(listPassword));
        entityToSave.setTimeCreated(LocalDateTime.now());
        entityToSave.setLastUsedTime(LocalDateTime.now());
        listRepo.save(entityToSave);

        String response = mockMvc.perform(post("/lists/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequestObject)))
                .andExpect(status().isUnauthorized())
                .andReturn().getResponse().getErrorMessage();

        assertNotNull(response);
        assertTrue(response.contains("empty"));
    }

    @Test
    void givenEmptyListName_whenCreateList_thenReturnBadRequest() throws Exception {
        String listName = "";
        String listPassword = "testPass";
        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("name", listName);
        requestObject.put("password", listPassword);


        String response = mockMvc.perform(post("/lists/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestObject))
        )
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertEquals(0, listRepo.count());
        assert response != null;
        assertTrue(response.contains("empty"));

    }

    @Test
    void givenBlankListName_whenCreateList_thenReturnBadRequest() throws Exception {
        String listName = "      ";
        String listPassword = "testPass";
        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("name", listName);
        requestObject.put("password", listPassword);


        String response = mockMvc.perform(post("/lists/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestObject))
        )
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertEquals(0, listRepo.count());
        assert response != null;
        assertTrue(response.contains("empty"));
    }

    @Test
    void givenEmptyPassword_whenCreateList_thenReturnBadRequest() throws Exception {
        String listName = "listName";
        String listPassword = "";
        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("name", listName);
        requestObject.put("password", listPassword);


        String response = mockMvc.perform(post("/lists/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestObject))
        )
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertEquals(0, listRepo.count());
        assert response != null;
        assertTrue(response.contains("empty"));
    }

    @Test
    void givenBlankPassword_whenCreateList_thenReturnBadRequest() throws Exception {
        String listName = "listName";
        String listPassword = "       ";
        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("name", listName);
        requestObject.put("password", listPassword);


        String response = mockMvc.perform(post("/lists/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestObject))
        )
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertEquals(0, listRepo.count());
        assert response != null;
        assertTrue(response.contains("empty"));
    }

    @Test
    void givenAlreadyExistingListName_whenCreateList_thenReturnBadRequest() throws Exception {
        String listName = "testList";
        String listPassword = "testPass";
        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("name", listName);
        requestObject.put("password", listPassword);
        ShoppingListEntity testEntity = new ShoppingListEntity();
        testEntity.setName(listName);
        testEntity.setPassword(listPassword);
        testEntity.setTimeCreated(LocalDateTime.now().minusDays(1));
        testEntity.setLastUsedTime(LocalDateTime.now().minusHours(8));
        listRepo.save(testEntity);


        assertEquals(1, listRepo.count());

        String response = mockMvc.perform(post("/lists/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestObject))
        )
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        ListAlreadyExists expectedException = new ListAlreadyExists(listName);

        assertEquals(response, expectedException.getMessage());
    }

    @Test
    void givenValidTooken_whenDeleteList_thenSetListPendingDeletion() throws Exception {
        String listName = "testList";
        String listPassword = "testListPassword";

        Map<String, String> listRequest = new HashMap<>();
        listRequest.put("name", listName);
        listRequest.put("password", listPassword);

        mockMvc.perform(post("/lists/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listRequest)))
                .andExpect(status().isOk());

        String loginResponse = mockMvc.perform(post("/lists/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listRequest)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        AuthTokenDTO authToken = objectMapper.readValue(loginResponse, AuthTokenDTO.class);

        String deleteResponse = mockMvc.perform(delete("/lists/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + authToken.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ShoppingListEntity actualList = listRepo.findByNameIgnoreCase(listName);

        assertTrue(deleteResponse.toLowerCase().contains("will be deleted"));
        assertTrue(actualList.isPendingDeletion());
        assertEquals(
                LocalDateTime.now().truncatedTo(ChronoUnit.HOURS)
                        .plusHours(service.getLIST_DELETION_GRACE_PERIOD()),
                actualList.getDeletionTime()
                        .truncatedTo(ChronoUnit.HOURS)
        );
    }


//    @Test
//    void givenValidtoken_whenDeleteList_thenDeleteListAndItems() throws Exception {
//        String listName = "testList";
//        String listPassword = "testListPassword";
//
//        Map<String, String> listRequest = new HashMap<>();
//        listRequest.put("name", listName);
//        listRequest.put("password", listPassword);
//
//        mockMvc.perform(post("/lists/create")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(listRequest)))
//                .andExpect(status().isOk());
//
//        String loginResponse = mockMvc.perform(post("/lists/login")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(listRequest)))
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        AuthTokenDTO authToken = objectMapper.readValue(loginResponse, AuthTokenDTO.class);
//
//        ShoppingItemDTO shoppingItem = new ShoppingItemDTO();
//        shoppingItem.setItemName("someName");
//
//        mockMvc.perform(post("/items/add")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(shoppingItem))
//                .header("Authorization", "Bearer " + authToken.getToken()))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//
//        String deleteResponse = mockMvc.perform(delete("/lists/delete")
//                .contentType(MediaType.APPLICATION_JSON)
//                .header("Authorization", "Bearer " + authToken.getToken()))
//                .andDo(print())
//                .andExpect(status().isOk())
//                .andReturn().getResponse().getContentAsString();
//
//        assertNull(listRepo.findByNameIgnoreCase(listName));
//        assertEquals(0, listRepo.count());
//
//        assertNull(listRepo.findByNameIgnoreCase(shoppingItem.getItemName()));
//        assertEquals(0, listRepo.count());
//    }

}