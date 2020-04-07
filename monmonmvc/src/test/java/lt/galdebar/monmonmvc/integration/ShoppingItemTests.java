package lt.galdebar.monmonmvc.integration;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lt.galdebar.monmon.categoriesparser.ExcelParserApp;
import lt.galdebar.monmon.categoriesparser.services.CategoriesParserMain;
import lt.galdebar.monmonmvc.persistence.domain.entities.ShoppingItemEntity;
import lt.galdebar.monmonmvc.persistence.domain.dto.LoginAttemptDTO;
import lt.galdebar.monmonmvc.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import javax.servlet.ServletContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
public class ShoppingItemTests {
    private static final String TEST_USER_EMAIL = "user@somemail.com";
    private static final String TEST_USER_PASS = "password";

    @TestConfiguration
    @ComponentScan(basePackages = "lt.galdebar.monmon.categoriesparser")
    @Import(CategoriesParserMain.class)
    public class Config{
        @Bean
        public CategoriesParserMain categoriesParserMain(){
            return new CategoriesParserMain();
        }
    }

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private CategoriesParserMain categoriesParserMain;

    @Autowired
    private
    ShoppingItemRepo shoppingItemRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mvc;

    private TestUserCreatorHelper userCreatorHelper;

    private ObjectMapper objectMapper;

    @Before
    public void setup() throws UserNotFound {
        this.mvc = MockMvcBuilders
                .webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();
        this.objectMapper = new ObjectMapper();
        userCreatorHelper = new TestUserCreatorHelper(userRepo, passwordEncoder);

        userCreatorHelper.createSimpleUser(TEST_USER_EMAIL, TEST_USER_PASS);

        categoriesParserMain.pushCategoriesToDB();
    }

    @After
    public void tearDown() {
        userCreatorHelper.clearDB();
        shoppingItemRepo.deleteAll();
    }

    @Test
    public void givenWac_whenServletContext_thenItProvidesShoppingItemController() {
        ServletContext servletContext = wac.getServletContext();

        assertNotNull(servletContext);
        assertTrue(servletContext instanceof MockServletContext);
        assertNotNull(wac.getBean("shoppingItemController"));
    }

    @Test
    public void givenItems_whenGetAllItems_thenReturnItemsArrayJson() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "water";
        String item1Category = "Beverages";
        String item2Name = "beer";
        createAndSaveShoppingItem(item1Name, item1Category, TEST_USER_EMAIL);
        createAndSaveShoppingItem(item2Name, item1Category, TEST_USER_EMAIL);

        mvc.perform(get("/shoppingitems/getAll").header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(item1Name)))
                .andExpect(content().string(containsString(item2Name)))
                .andExpect(content().string(containsString(item1Category)));

    }

    @Test
    public void givenIncorrectUser_whenGetAll_thenReturnEmptyArray() throws Exception, UserNotFound {
        String testUserName = "emptyUser@mail.com";
        String testUserPassword = "password";
        userCreatorHelper.createSimpleUser(testUserName, testUserPassword);
        String authToken = getAuthToken(testUserName, testUserPassword);
        String expectedEmptyArray = "[]";

        String item1Name = "water";
        String item1Category = "Beverages";
        String item2Name = "beer";
        createAndSaveShoppingItem(item1Name, item1Category, TEST_USER_EMAIL);
        createAndSaveShoppingItem(item2Name, item1Category, TEST_USER_EMAIL);

        mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedEmptyArray));
    }

    @Test
    public void givenTwoUsers_whenGetAllForEach_thenReturnDifferentResponses() throws Exception, UserNotFound {
        String userAEmail = "userA@mail.com";
        String userAPassword = "pass";
        String userBEmail = "emptyUser@mail.com";
        String userBPassword = "password";
        userCreatorHelper.createSimpleUser(userAEmail, userAPassword);
        userCreatorHelper.createSimpleUser(userBEmail, userBPassword);
        String emptyResponse = "[]";


        String item1Name = "water";
        String item1Category = "Beverages";
        String item2Name = "beer";
        createAndSaveShoppingItem(item1Name, item1Category, userAEmail);
        createAndSaveShoppingItem(item2Name, item1Category, userBEmail);


        String userAAuthToken = getAuthToken(userAEmail, userAPassword);
        String userBAuthToken = getAuthToken(userBEmail, userBPassword);

        String userAresponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + userAAuthToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        String userBresponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + userBAuthToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(userAresponse);
        assertNotNull(userBresponse);
        assertNotEquals(userAresponse, emptyResponse);
        assertNotEquals(userBresponse, emptyResponse);
        assertTrue(userAresponse.contains(item1Name));
        assertFalse(userAresponse.contains(item2Name));
        assertTrue(userBresponse.contains(item2Name));
        assertFalse(userBresponse.contains(item1Name));

    }

    @Test
    public void givenLinkedUsers_whenGetAllForBothUsers_thenReturnIdenticalJson() throws UserNotFound, Exception {
        String userAEmail = "userA@mail.com";
        String userAPassword = "pass";
        String userBEmail = "emptyUser@mail.com";
        String userBPassword = "password";
        userCreatorHelper.createSimpleUser(userAEmail, userAPassword);
        userCreatorHelper.createSimpleUser(userBEmail, userBPassword);
        String emptyResponse = "[]";

        userCreatorHelper.createLinkedUsers(userAEmail, userBEmail);

        String item1Name = "water";
        String item1Category = "Beverages";
        String item2Name = "beer";
        createAndSaveShoppingItem(item1Name, item1Category, userAEmail);
        createAndSaveShoppingItem(item2Name, item1Category, userAEmail);


        String userAAuthToken = getAuthToken(userAEmail, userAPassword);
        String userBAuthToken = getAuthToken(userBEmail, userBPassword);

        String userAresponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + userAAuthToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        String userBresponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + userBAuthToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(userAresponse);
        assertNotNull(userBresponse);
        assertNotEquals(userAresponse, emptyResponse);
        assertNotEquals(userBresponse, emptyResponse);
        assertEquals(userAresponse, userBresponse);
    }

    @Test
    public void givenTwoLinkedUsersWithDifferentItems_whenGetAll_thenReturnEqualResponses() throws UserNotFound, Exception {
        String userAEmail = "userA@mail.com";
        String userAPassword = "pass";
        String userBEmail = "emptyUser@mail.com";
        String userBPassword = "password";
        userCreatorHelper.createSimpleUser(userAEmail, userAPassword);
        userCreatorHelper.createSimpleUser(userBEmail, userBPassword);
        String emptyResponse = "[]";


        userCreatorHelper.createLinkedUsers(userAEmail, userBEmail);

        String item1Name = "water";
        String item1Category = "Beverages";
        String item2Name = "beer";
        createAndSaveShoppingItem(item1Name, item1Category, userAEmail);
        createAndSaveShoppingItem(item2Name, item1Category, userBEmail);


        String userAAuthToken = getAuthToken(userAEmail, userAPassword);
        String userBAuthToken = getAuthToken(userBEmail, userBPassword);

        String userAresponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + userAAuthToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();
        String userBresponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + userBAuthToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertNotNull(userAresponse);
        assertNotNull(userBresponse);
        assertNotEquals(userAresponse, emptyResponse);
        assertNotEquals(userBresponse, emptyResponse);
        assertEquals(userAresponse, userBresponse);

    }

    @Test
    public void givenItemWithNoCategory_whenAddItem_thenReturnUncategorizedItem() throws Exception {
        String itemName = "itemName";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("itemName", itemName);
        requestObject.put("itemCategory", "");
        requestObject.put("quantity", "0");
        requestObject.put("comment", "");
        requestObject.put("isInCart", "false");

        String expectedCategory = "Uncategorized";
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String response = mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(
                        objectMapper.writeValueAsString(requestObject)
                )
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertFalse(response.contentEquals("[]"));
        assertTrue(response.contains(itemName));
        assertTrue(response.contains(expectedCategory));

    }

    @Test
    public void givenItemWithSpecificCategory_whenAddItem_thenReturnItemWithSameCategory() throws Exception {
        String itemName = "itemName";
        String itemCategory = "Bakery";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("itemName", itemName);
        requestObject.put("itemCategory", itemCategory);
        requestObject.put("quantity", "0");
        requestObject.put("comment", "");
        requestObject.put("isInCart", "false");

        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String response = mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(
                        objectMapper.writeValueAsString(requestObject)
                )
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertFalse(response.contentEquals("[]"));
        assertTrue(response.contains(itemName));
        assertTrue(response.contains(itemCategory));
    }

    @Test
    public void givenNoCategory_whenAddItem_thenReturnItemWithAssignedCategory() throws Exception {
        String itemName = "water";
        String itemCategory = "";
        String expectedCategory = "Beverages";


        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("itemName", itemName);
        requestObject.put("itemCategory", itemCategory);
        requestObject.put("quantity", "0");
        requestObject.put("comment", "");
        requestObject.put("isInCart", "false");

        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String response = mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(
                        objectMapper.writeValueAsString(requestObject)
                )
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertFalse(response.contentEquals("[]"));
        assertTrue(response.contains(itemName));
        assertTrue(response.contains(expectedCategory));
    }

    @Test
    public void givenIncorrectCategory_whenAddItem_thenReturnUncategorized() throws Exception {
        String itemName = "water";
        String itemCategory = "Bevferadges";
        String expectedCategory = "Uncategorized";


        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("itemName", itemName);
        requestObject.put("itemCategory", itemCategory);
        requestObject.put("quantity", "0");
        requestObject.put("comment", "");
        requestObject.put("isInCart", "false");

        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String response = mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(
                        objectMapper.writeValueAsString(requestObject)
                )
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertFalse(response.contentEquals("[]"));
        assertTrue(response.contains(itemName));
        assertTrue(response.contains(expectedCategory));
    }

    @Test
    public void givenEmptyRequest_whenAddItem_thenReturnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenEmptyItem_whenAddItem_thenReturnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String itemName = "";

        Map requestObject = createDefaultRequestObject(itemName);

        mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenBlankItem_whenAddItem_thenReturnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String itemName = "     ";

        Map requestObject = createDefaultRequestObject(itemName);

        mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenStandardItem_whenUpdateItemAllFields_returnUpdatedItem() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String itemName = "itemName";

        Map<String, String> requestObject = createDefaultRequestObject(itemName);
        String responseString = pushRequestObjToDB(requestObject,authToken);
        JsonNode jsonNode = objectMapper.readTree(responseString);
        String itemId = jsonNode.get("id").asText();

        String updatedName = "updatedName";
        String updatedCategory = "Deli";
        int updatedQuantity = 22;
        String updatedComment = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject = new HashMap<>();
        updateRequestObject.put("id", itemId);
        updateRequestObject.put("itemName", updatedName);
        updateRequestObject.put("itemCategory", updatedCategory);
        updateRequestObject.put("quantity", Integer.toString(updatedQuantity));
        updateRequestObject.put("comment", updatedComment);
        updateRequestObject.put("isInCart", Boolean.toString(updatedIsInCart));

        String updateResponse = mvc.perform(put("/shoppingitems/updateitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(updateRequestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertFalse(updateResponse.trim().isEmpty());
        assertEquals(itemId, objectMapper
                .readTree(updateResponse)
                .get("id").asText());
        assertEquals(updatedName, objectMapper
                .readTree(updateResponse)
                .get("itemName").asText());
        assertEquals(updatedCategory, objectMapper
                .readTree(updateResponse)
                .get("itemCategory").asText());
        assertEquals(Integer.toString(updatedQuantity), objectMapper
                .readTree(updateResponse)
                .get("quantity").asText());
        assertEquals(updatedComment, objectMapper
                .readTree(updateResponse)
                .get("comment").asText());
        assertEquals(Boolean.toString(updatedIsInCart), objectMapper
                .readTree(updateResponse)
                .get("isInCart").asText());
    }

    @Test
    public void givenEmptyItem_whenUpdateItemAllFields_returnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String itemName = "itemName";

        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("itemName", itemName);
        requestObject.put("itemCategory", "");
        requestObject.put("quantity", "0");
        requestObject.put("comment", "");
        requestObject.put("isInCart", "false");

        String responseString = mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseString);
        String itemId = jsonNode.get("id").asText();

        String updatedName = "";
        String updatedCategory = "Deli";
        int updatedQuantity = 22;
        String updatedComment = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject = new HashMap<>();
        updateRequestObject.put("id", itemId);
        updateRequestObject.put("itemName", updatedName);
        updateRequestObject.put("itemCategory", updatedCategory);
        updateRequestObject.put("quantity", Integer.toString(updatedQuantity));
        updateRequestObject.put("comment", updatedComment);
        updateRequestObject.put("isInCart", Boolean.toString(updatedIsInCart));

        mvc.perform(put("/shoppingitems/updateitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(updateRequestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    public void givenBlankItem_whenUpdateItemAllFields_returnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String itemName = "itemName";

        Map<String, String> requestObject = createDefaultRequestObject(itemName);
        String responseString = pushRequestObjToDB(requestObject,authToken);
        JsonNode jsonNode = objectMapper.readTree(responseString);
        String itemId = jsonNode.get("id").asText();

        String updatedName = "     ";
        String updatedCategory = "Deli";
        int updatedQuantity = 22;
        String updatedComment = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject = new HashMap<>();
        updateRequestObject.put("id", itemId);
        updateRequestObject.put("itemName", updatedName);
        updateRequestObject.put("itemCategory", updatedCategory);
        updateRequestObject.put("quantity", Integer.toString(updatedQuantity));
        updateRequestObject.put("comment", updatedComment);
        updateRequestObject.put("isInCart", Boolean.toString(updatedIsInCart));

        mvc.perform(put("/shoppingitems/updateitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(updateRequestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    public void givenItemWithIncorrectID_whenUpdateItemAllFields_returnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String itemName = "itemName";

        Map<String, String> requestObject = createDefaultRequestObject(itemName);
        pushRequestObjToDB(requestObject,authToken);

        String itemId = "liauwjgdkaijwhgdlijahwd";

        String updatedName = "updatedName";
        String updatedCategory = "Deli";
        int updatedQuantity = 22;
        String updatedComment = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject = new HashMap<>();
        updateRequestObject.put("id", itemId);
        updateRequestObject.put("itemName", updatedName);
        updateRequestObject.put("itemCategory", updatedCategory);
        updateRequestObject.put("quantity", Integer.toString(updatedQuantity));
        updateRequestObject.put("comment", updatedComment);
        updateRequestObject.put("isInCart", Boolean.toString(updatedIsInCart));

        mvc.perform(put("/shoppingitems/updateitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(updateRequestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    public void givenStandardItems_whenUpdateItemAllFieldsAndGetAll_returnUpdatedItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        String responseString1 = pushRequestObjToDB(requestObject1, authToken);
        JsonNode jsonNode1 = objectMapper.readTree(responseString1);
        String item1Id = jsonNode1.get("id").asText();

        String responseString2 = pushRequestObjToDB(requestObject2, authToken);
        JsonNode jsonNode2 = objectMapper.readTree(responseString2);
        String item2Id = jsonNode2.get("id").asText();

        String updatedName1 = "updatedName";
        String updatedCategory1 = "Deli";
        int updatedQuantity1 = 22;
        String updatedComment1 = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject1 = new HashMap<>();
        updateRequestObject1.put("id", item1Id);
        updateRequestObject1.put("itemName", updatedName1);
        updateRequestObject1.put("itemCategory", updatedCategory1);
        updateRequestObject1.put("quantity", Integer.toString(updatedQuantity1));
        updateRequestObject1.put("comment", updatedComment1);
        updateRequestObject1.put("isInCart", Boolean.toString(updatedIsInCart));

        String updatedName2 = "updatedName2";
        String updatedCategory2 = "Beverages";
        int updatedQuantity2 = 11;
        String updatedComment2 = "this is another comment";

        Map<String, String> updateRequestObject2 = new HashMap<>();
        updateRequestObject2.put("id", item2Id);
        updateRequestObject2.put("itemName", updatedName2);
        updateRequestObject2.put("itemCategory", updatedCategory2);
        updateRequestObject2.put("quantity", Integer.toString(updatedQuantity2));
        updateRequestObject2.put("comment", updatedComment2);
        updateRequestObject2.put("isInCart", Boolean.toString(updatedIsInCart));

        List<Map<String, String>> listUpdateObject = new ArrayList<>();
        listUpdateObject.add(updateRequestObject1);
        listUpdateObject.add(updateRequestObject2);

        mvc.perform(put("/shoppingitems/updateitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listUpdateObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String getAllResponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(updateRequestObject1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(getAllResponse.contains(updatedName1));
        assertTrue(getAllResponse.contains(updatedName2));
        assertTrue(getAllResponse.contains(updatedCategory1));
        assertTrue(getAllResponse.contains(updatedCategory2));
        assertTrue(getAllResponse.contains(updatedComment1));
        assertTrue(getAllResponse.contains(updatedComment2));

    }

    @Test
    public void givenStandardItemAndOneWithIncorrectID_whenUpdateItemAllFieldsAndGetAll_returnUpdatedItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        String responseString1 = pushRequestObjToDB(requestObject1, authToken);
        JsonNode jsonNode1 = objectMapper.readTree(responseString1);
        String item1Id = jsonNode1.get("id").asText();

        pushRequestObjToDB(requestObject2, authToken);

        String item2Id = "iauhwdkjhawd";

        String updatedName1 = "updatedName";
        String updatedCategory1 = "Deli";
        int updatedQuantity1 = 22;
        String updatedComment1 = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject1 = new HashMap<>();
        updateRequestObject1.put("id", item1Id);
        updateRequestObject1.put("itemName", updatedName1);
        updateRequestObject1.put("itemCategory", updatedCategory1);
        updateRequestObject1.put("quantity", Integer.toString(updatedQuantity1));
        updateRequestObject1.put("comment", updatedComment1);
        updateRequestObject1.put("isInCart", Boolean.toString(updatedIsInCart));

        String updatedName2 = "updatedName2";
        String updatedCategory2 = "Beverages";
        int updatedQuantity2 = 11;
        String updatedComment2 = "this is another comment";

        Map<String, String> updateRequestObject2 = new HashMap<>();
        updateRequestObject2.put("id", item2Id);
        updateRequestObject2.put("itemName", updatedName2);
        updateRequestObject2.put("itemCategory", updatedCategory2);
        updateRequestObject2.put("quantity", Integer.toString(updatedQuantity2));
        updateRequestObject2.put("comment", updatedComment2);
        updateRequestObject2.put("isInCart", Boolean.toString(updatedIsInCart));

        List<Map<String, String>> listUpdateObject = new ArrayList<>();
        listUpdateObject.add(updateRequestObject1);
        listUpdateObject.add(updateRequestObject2);

        mvc.perform(put("/shoppingitems/updateitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listUpdateObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenStandardItemAndOneWithEmptyID_whenUpdateItemAllFieldsAndGetAll_returnUpdatedItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        String responseString1 = pushRequestObjToDB(requestObject1, authToken);
        JsonNode jsonNode1 = objectMapper.readTree(responseString1);
        String item1Id = jsonNode1.get("id").asText();

        pushRequestObjToDB(requestObject2, authToken);

        String item2Id = "";

        String updatedName1 = "updatedName";
        String updatedCategory1 = "Deli";
        int updatedQuantity1 = 22;
        String updatedComment1 = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject1 = new HashMap<>();
        updateRequestObject1.put("id", item1Id);
        updateRequestObject1.put("itemName", updatedName1);
        updateRequestObject1.put("itemCategory", updatedCategory1);
        updateRequestObject1.put("quantity", Integer.toString(updatedQuantity1));
        updateRequestObject1.put("comment", updatedComment1);
        updateRequestObject1.put("isInCart", Boolean.toString(updatedIsInCart));

        String updatedName2 = "updatedName2";
        String updatedCategory2 = "Beverages";
        int updatedQuantity2 = 11;
        String updatedComment2 = "this is another comment";

        Map<String, String> updateRequestObject2 = new HashMap<>();
        updateRequestObject2.put("id", item2Id);
        updateRequestObject2.put("itemName", updatedName2);
        updateRequestObject2.put("itemCategory", updatedCategory2);
        updateRequestObject2.put("quantity", Integer.toString(updatedQuantity2));
        updateRequestObject2.put("comment", updatedComment2);
        updateRequestObject2.put("isInCart", Boolean.toString(updatedIsInCart));

        List<Map<String, String>> listUpdateObject = new ArrayList<>();
        listUpdateObject.add(updateRequestObject1);
        listUpdateObject.add(updateRequestObject2);

        mvc.perform(put("/shoppingitems/updateitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listUpdateObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenStandardItemAndOneWithBlankID_whenUpdateItemAllFieldsAndGetAll_returnUpdatedItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        String responseString1 = pushRequestObjToDB(requestObject1, authToken);
        JsonNode jsonNode1 = objectMapper.readTree(responseString1);
        String item1Id = jsonNode1.get("id").asText();

        pushRequestObjToDB(requestObject2, authToken);

        String item2Id = "     ";

        String updatedName1 = "updatedName";
        String updatedCategory1 = "Deli";
        int updatedQuantity1 = 22;
        String updatedComment1 = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject1 = new HashMap<>();
        updateRequestObject1.put("id", item1Id);
        updateRequestObject1.put("itemName", updatedName1);
        updateRequestObject1.put("itemCategory", updatedCategory1);
        updateRequestObject1.put("quantity", Integer.toString(updatedQuantity1));
        updateRequestObject1.put("comment", updatedComment1);
        updateRequestObject1.put("isInCart", Boolean.toString(updatedIsInCart));

        String updatedName2 = "updatedName2";
        String updatedCategory2 = "Beverages";
        int updatedQuantity2 = 11;
        String updatedComment2 = "this is another comment";

        Map<String, String> updateRequestObject2 = new HashMap<>();
        updateRequestObject2.put("id", item2Id);
        updateRequestObject2.put("itemName", updatedName2);
        updateRequestObject2.put("itemCategory", updatedCategory2);
        updateRequestObject2.put("quantity", Integer.toString(updatedQuantity2));
        updateRequestObject2.put("comment", updatedComment2);
        updateRequestObject2.put("isInCart", Boolean.toString(updatedIsInCart));

        List<Map<String, String>> listUpdateObject = new ArrayList<>();
        listUpdateObject.add(updateRequestObject1);
        listUpdateObject.add(updateRequestObject2);

        mvc.perform(put("/shoppingitems/updateitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listUpdateObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenStandardItemAndOneWithNullID_whenUpdateItemAllFieldsAndGetAll_returnUpdatedItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        String responseString1 = pushRequestObjToDB(requestObject1, authToken);
        JsonNode jsonNode1 = objectMapper.readTree(responseString1);
        String item1Id = jsonNode1.get("id").asText();

        pushRequestObjToDB(requestObject2, authToken);

        String item2Id = null;

        String updatedName1 = "updatedName";
        String updatedCategory1 = "Deli";
        int updatedQuantity1 = 22;
        String updatedComment1 = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject1 = new HashMap<>();
        updateRequestObject1.put("id", item1Id);
        updateRequestObject1.put("itemName", updatedName1);
        updateRequestObject1.put("itemCategory", updatedCategory1);
        updateRequestObject1.put("quantity", Integer.toString(updatedQuantity1));
        updateRequestObject1.put("comment", updatedComment1);
        updateRequestObject1.put("isInCart", Boolean.toString(updatedIsInCart));

        String updatedName2 = "updatedName2";
        String updatedCategory2 = "Beverages";
        int updatedQuantity2 = 11;
        String updatedComment2 = "this is another comment";

        Map<String, String> updateRequestObject2 = new HashMap<>();
        updateRequestObject2.put("id", item2Id);
        updateRequestObject2.put("itemName", updatedName2);
        updateRequestObject2.put("itemCategory", updatedCategory2);
        updateRequestObject2.put("quantity", Integer.toString(updatedQuantity2));
        updateRequestObject2.put("comment", updatedComment2);
        updateRequestObject2.put("isInCart", Boolean.toString(updatedIsInCart));

        List<Map<String, String>> listUpdateObject = new ArrayList<>();
        listUpdateObject.add(updateRequestObject1);
        listUpdateObject.add(updateRequestObject2);

        mvc.perform(put("/shoppingitems/updateitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listUpdateObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void givenEmptyRequest_whenUpdateItemAllFieldsAndGetAll_returnUpdatedItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        pushRequestObjToDB(requestObject1, authToken);
        pushRequestObjToDB(requestObject2, authToken);


        mvc.perform(put("/shoppingitems/updateitems")
                .header("Authorization", "Bearer " + authToken)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void givenItemWithEmptyID_whenUpdateItemAllFields_returnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String itemName = "itemName";

        Map<String, String> requestObject = createDefaultRequestObject(itemName);
        pushRequestObjToDB(requestObject, authToken);

        String itemId = "";

        String updatedName = "updatedName";
        String updatedCategory = "Deli";
        int updatedQuantity = 22;
        String updatedComment = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject = new HashMap<>();
        updateRequestObject.put("id", itemId);
        updateRequestObject.put("itemName", updatedName);
        updateRequestObject.put("itemCategory", updatedCategory);
        updateRequestObject.put("quantity", Integer.toString(updatedQuantity));
        updateRequestObject.put("comment", updatedComment);
        updateRequestObject.put("isInCart", Boolean.toString(updatedIsInCart));

        mvc.perform(put("/shoppingitems/updateitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(updateRequestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    public void givenItemWithBlankID_whenUpdateItemAllFields_returnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String itemName = "itemName";

        Map<String, String> requestObject = createDefaultRequestObject(itemName);
        pushRequestObjToDB(requestObject, authToken);

        String itemId = "      ";

        String updatedName = "updatedName";
        String updatedCategory = "Deli";
        int updatedQuantity = 22;
        String updatedComment = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject = new HashMap<>();
        updateRequestObject.put("id", itemId);
        updateRequestObject.put("itemName", updatedName);
        updateRequestObject.put("itemCategory", updatedCategory);
        updateRequestObject.put("quantity", Integer.toString(updatedQuantity));
        updateRequestObject.put("comment", updatedComment);
        updateRequestObject.put("isInCart", Boolean.toString(updatedIsInCart));

        mvc.perform(put("/shoppingitems/updateitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(updateRequestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    public void givenItemWithNullID_whenUpdateItemAllFields_returnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String itemName = "itemName";

        Map<String, String> requestObject = createDefaultRequestObject(itemName);
        pushRequestObjToDB(requestObject, authToken);

        String itemId = null;

        String updatedName = "updatedName";
        String updatedCategory = "Deli";
        int updatedQuantity = 22;
        String updatedComment = "this is a comment";
        boolean updatedIsInCart = true;

        Map<String, String> updateRequestObject = new HashMap<>();
        updateRequestObject.put("id", itemId);
        updateRequestObject.put("itemName", updatedName);
        updateRequestObject.put("itemCategory", updatedCategory);
        updateRequestObject.put("quantity", Integer.toString(updatedQuantity));
        updateRequestObject.put("comment", updatedComment);
        updateRequestObject.put("isInCart", Boolean.toString(updatedIsInCart));

        mvc.perform(put("/shoppingitems/updateitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(updateRequestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andDo(print());

    }

    @Test
    public void givenMultipleItems_whenDeleteItemAndGetAllItems_thenReturnRemainingItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = new HashMap<>();
        requestObject1.put("itemName", item1Name);
        requestObject1.put("itemCategory", "");
        requestObject1.put("quantity", "0");
        requestObject1.put("comment", "");
        requestObject1.put("isInCart", "false");

        Map<String, String> requestObject2 = new HashMap<>();
        requestObject2.put("itemName", item2Name);
        requestObject2.put("itemCategory", "");
        requestObject2.put("quantity", "0");
        requestObject2.put("comment", "");
        requestObject2.put("isInCart", "false");

        String responseString = mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject2))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseString);
        String itemId = jsonNode.get("id").asText();
        requestObject1.put("id", itemId);

        mvc.perform(delete("/shoppingitems/deleteitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        String getAllResponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + authToken))
                .andExpect(status().isOk())
                .andDo(print())
                .andReturn().getResponse().getContentAsString();

        assertNotEquals(getAllResponse, "");
        assertTrue(getAllResponse.contains(item2Name));
        assertFalse(getAllResponse.contains(item1Name));
    }

    @Test
    public void givenIncorrectID_whenDeleteItemAndGetAllItems_thenReturnRemainingItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        pushRequestObjToDB(requestObject1, authToken);
        pushRequestObjToDB(requestObject2, authToken);

        String itemId = "liujahwdlijd";
        requestObject1.put("id", itemId);

        mvc.perform(delete("/shoppingitems/deleteitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenEmptyID_whenDeleteItemAndGetAllItems_thenReturnRemainingItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        pushRequestObjToDB(requestObject1, authToken);
        pushRequestObjToDB(requestObject2, authToken);

        String itemId = "";
        requestObject1.put("id", itemId);

        mvc.perform(delete("/shoppingitems/deleteitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenBlankID_whenDeleteItemAndGetAllItems_thenReturnRemainingItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        pushRequestObjToDB(requestObject1, authToken);
        pushRequestObjToDB(requestObject2, authToken);

        String itemId = "  ";
        requestObject1.put("id", itemId);

        mvc.perform(delete("/shoppingitems/deleteitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenNullID_whenDeleteItemAndGetAllItems_thenReturnRemainingItems() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        pushRequestObjToDB(requestObject1, authToken);
        pushRequestObjToDB(requestObject2, authToken);

        String itemId = null;
        requestObject1.put("id", itemId);

        mvc.perform(delete("/shoppingitems/deleteitem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject1))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getContentAsString();
    }

    @Test
    public void givenMultipleItems_whenDeleteItemsAndGetAllItems_thenReturnEmptyArray() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);

        pushRequestObjToDBAndAddID(requestObject1, authToken);
        pushRequestObjToDBAndAddID(requestObject2, authToken);


        List<Map<String, String>> listDeleteObject = new ArrayList<>();
        listDeleteObject.add(requestObject1);
        listDeleteObject.add(requestObject2);

        mvc.perform(delete("/shoppingitems/deleteitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listDeleteObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        String getAllResponce = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listDeleteObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertEquals("[]", getAllResponce);
    }

    @Test
    public void givenMultipleItemsWithOneIncorrectID_whenDeleteItemsAndGetAllItems_thenReturnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);
        String responseString1 = pushRequestObjToDB(requestObject1, authToken);

        String item1Id = "iauwghdkjhawd";
        requestObject1.put("id", item1Id);

        pushRequestObjToDBAndAddID(requestObject2, authToken);


        List<Map<String, String>> listDeleteObject = new ArrayList<>();
        listDeleteObject.add(requestObject1);
        listDeleteObject.add(requestObject2);

        mvc.perform(delete("/shoppingitems/deleteitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listDeleteObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void givenMultipleItemsWithOneEmptyID_whenDeleteItemsAndGetAllItems_thenReturnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);
        String responseString1 = pushRequestObjToDB(requestObject1, authToken);

        String item1Id = "";
        requestObject1.put("id", item1Id);

        pushRequestObjToDBAndAddID(requestObject2, authToken);


        List<Map<String, String>> listDeleteObject = new ArrayList<>();
        listDeleteObject.add(requestObject1);
        listDeleteObject.add(requestObject2);

        mvc.perform(delete("/shoppingitems/deleteitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listDeleteObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        String getAllResponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(getAllResponse.contains(item1Name));
        assertTrue(getAllResponse.contains(item2Name));

    }

    @Test
    public void givenMultipleItemsWithOneBlankID_whenDeleteItemsAndGetAllItems_thenReturnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);
        String responseString1 = pushRequestObjToDB(requestObject1, authToken);

        String item1Id = "    ";
        requestObject1.put("id", item1Id);

        pushRequestObjToDBAndAddID(requestObject2, authToken);

        List<Map<String, String>> listDeleteObject = new ArrayList<>();
        listDeleteObject.add(requestObject1);
        listDeleteObject.add(requestObject2);

        mvc.perform(delete("/shoppingitems/deleteitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listDeleteObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        String getAllResponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(getAllResponse.contains(item1Name));
        assertTrue(getAllResponse.contains(item2Name));

    }

    @Test
    public void givenMultipleItemsWithOneNullID_whenDeleteItemsAndGetAllItems_thenReturnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        Map<String, String> requestObject1 = createDefaultRequestObject(item1Name);
        Map<String, String> requestObject2 = createDefaultRequestObject(item2Name);
        String responseString1 = pushRequestObjToDB(requestObject1, authToken);

        String item1Id = null;
        requestObject1.put("id", item1Id);

        pushRequestObjToDBAndAddID(requestObject2, authToken);

        List<Map<String, String>> listDeleteObject = new ArrayList<>();
        listDeleteObject.add(requestObject1);
        listDeleteObject.add(requestObject2);

        mvc.perform(delete("/shoppingitems/deleteitems")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(listDeleteObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        String getAllResponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(getAllResponse.contains(item1Name));
        assertTrue(getAllResponse.contains(item2Name));

    }

    @Test
    public void givenBlankRequest_whenDeleteItemsAndGetAllItems_thenReturnBadRequest() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String item1Name = "item1Name";
        String item2Name = "item2Name";

        createAndSaveDefaultRequestObject(item1Name, authToken);
        createAndSaveDefaultRequestObject(item2Name, authToken);


        mvc.perform(delete("/shoppingitems/deleteitems")
                .header("Authorization", "Bearer " + authToken)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        String getAllResponse = mvc.perform(get("/shoppingitems/getAll")
                .header("Authorization", "Bearer " + authToken)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        assertTrue(getAllResponse.contains(item1Name));
        assertTrue(getAllResponse.contains(item2Name));

    }

    //tests with empty requests ??

    private String getAuthToken(String userName, String userPassword) throws Exception {
        LoginAttemptDTO loginAttemptDTO = new LoginAttemptDTO();
        loginAttemptDTO.setUserEmail(userName);
        loginAttemptDTO.setUserPassword(userPassword);

        String resultString = mvc.perform(
                post("/user/login")
                        .contentType("application/json")
                        .content(new ObjectMapper().writeValueAsString(loginAttemptDTO))
                        .header("Content-Type", "application/json")
        ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();

        JacksonJsonParser jsonParser = new JacksonJsonParser();
        return jsonParser.parseMap(resultString).get("token").toString();
    }

    private ShoppingItemEntity createAndSaveShoppingItem(String itemName, String itemCategory, String user) {
        ShoppingItemEntity item1 = new ShoppingItemEntity();
        item1.itemName = itemName;
        item1.itemCategory = itemCategory;
        item1.quantity = 0;
        item1.isInCart = false;
        item1.users.add(user);

        return shoppingItemRepo.save(item1);
    }

    private Map createDefaultRequestObject(String itemName) {
        Map<String, String> requestObject = new HashMap<>();
        requestObject.put("itemName", itemName);
        requestObject.put("itemCategory", "");
        requestObject.put("quantity", "0");
        requestObject.put("comment", "");
        requestObject.put("isInCart", "false");
        return requestObject;
    }

    private String pushRequestObjToDB(Map<String, String> requestObject, String authToken) throws Exception {
        String responseString = mvc.perform(post("/shoppingitems/additem")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return responseString;
    }

    private Map<String, String> pushRequestObjToDBAndAddID(Map<String, String> requestObject, String authToken) throws Exception {
        String responseString = pushRequestObjToDB(requestObject, authToken);

        JsonNode jsonNode2 = objectMapper.readTree(responseString);
        String item2Id = jsonNode2.get("id").asText();
        requestObject.put("id", item2Id);
        return requestObject;
    }

    private String createAndSaveDefaultRequestObject(String itemName, String authToken) throws Exception {
        return pushRequestObjToDB(createDefaultRequestObject(itemName), authToken);
    }
}
