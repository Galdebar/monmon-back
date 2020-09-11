package lt.galdebar.monmonapi.app.api;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.app.context.security.AuthTokenDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemEntity;
import lt.galdebar.monmonapi.app.persistence.domain.shoppinglists.ShoppingListEntity;
import lt.galdebar.monmonapi.app.persistence.repositories.ShoppingItemRepo;
import lt.galdebar.monmonapi.app.persistence.repositories.ShoppingListRepo;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
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
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@TestPropertySource(locations = "classpath:test.properties")
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RunScraper.class)})
class ShoppingItemControllerTest {

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
    void setup() {
        itemRepo.deleteAll();
        listRepo.deleteAll();
    }

    @Test
    void contextLoads() {
        assertNotNull(itemsController);
    }


    @Test
    void whenGetAll_thenReturnArrayOfItemsOnlyForCurrentList() throws Exception {
        String testListName = "testList";
        String testListPassword = "testListPassword";
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO(testListName, testListPassword);

        String altListName = "altList";
        createListAndLoginAndGetAuthTokenDTO(altListName, testListPassword);

        String testItemName1 = "item1";
        String testItemName2 = "item2";
        ShoppingListEntity testListEntity = listRepo.findByNameIgnoreCase(testListName);
        ShoppingListEntity altListEntity = listRepo.findByNameIgnoreCase(altListName);

        ShoppingItemEntity testItem1 = new ShoppingItemEntity();
        testItem1.setComment("");
        testItem1.setInCart(false);
        testItem1.setQuantity(1);
        testItem1.setItemCategory("");
        testItem1.setItemName(testItemName1);
        testItem1.setShoppingList(testListEntity);
        ShoppingItemEntity testItem2 = new ShoppingItemEntity();
        testItem2.setComment("");
        testItem2.setInCart(false);
        testItem2.setQuantity(1);
        testItem2.setItemCategory("");
        testItem2.setItemName(testItemName2);
        testItem2.setShoppingList(testListEntity);

        ShoppingItemEntity testItem3 = new ShoppingItemEntity();
        testItem3.setComment("");
        testItem3.setInCart(true);
        testItem3.setQuantity(1);
        testItem3.setItemCategory("");
        testItem3.setItemName(testItemName1);
        testItem3.setShoppingList(altListEntity);

        itemRepo.save(testItem1);
        itemRepo.save(testItem2);
        itemRepo.save(testItem3);
        listRepo.save(testListEntity);
        listRepo.save(altListEntity);


        String response = mockMvc.perform(get("/items/getall")
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ShoppingItemDTO> actualItems = objectMapper.readValue(response, new TypeReference<List<ShoppingItemDTO>>() {
        });
        assertNotNull(response);
        assertEquals(2, actualItems.size());

    }

    @Test
    void givenNoAuthToken_whenGetAllItems_thenReturnForbidden() throws Exception {
        mockMvc.perform(get("/items/getall")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    @Test
    void givenValidItem_whenAddItem_thenItemAddedToDBandReturnSameItem() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();

        String testItemName = "testItem";
        ShoppingItemDTO expectedItem = new ShoppingItemDTO();
        expectedItem.setItemName(testItemName);
        Map<String, String> postRequest = new HashMap<>();
        postRequest.put("itemName", testItemName);
        postRequest.put("itemCategory", "");
        postRequest.put("quantity", "1");
        postRequest.put("comment", "");

        String response = mockMvc.perform(post("/items/add")
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

    @Test
    void givenNoAuthToken_whenAddItem_thenReturnForbidden() throws Exception {
        String itemName = "testItem";
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(itemName);

        mockMvc.perform(post("/items/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenNullItemName_whenAddItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();

        String testItemName = null;
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(testItemName);

        String response = mockMvc.perform(post("/items/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("name"));

    }

    @Test
    void givenBlankItemName_whenAddItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();

        String testItemName = "";
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(testItemName);

        String response = mockMvc.perform(post("/items/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("name"));

    }

    @Test
    void givenEmptyItemName_whenAddItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();

        String testItemName = "          ";
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(testItemName);

        String response = mockMvc.perform(post("/items/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("name"));

    }

    @Test
    void givenInvalidQuantity_whenAddItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();

        String testItemName = "ooiahwd";
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(testItemName);
        testItem.setQuantity(-2);

        String response = mockMvc.perform(post("/items/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("quantity"));

    }

    //later check addinng item with incorrect category ?

    @Test
    void givenValidItem_whenUpdateItem_thenReturnUpdatedItemAndUpdateDB() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        String updatedItemCategory = "someCategory";
        String updatedItemComment = "oiuaogwhldiuhuga";
        Integer updatedItemQuantity = 10;
        boolean updatedItemIsInCart = true;

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = itemRepo.save(itemToSave).getId();

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName(testItemName);
        requestItem.setItemCategory(updatedItemCategory);
        requestItem.setQuantity(updatedItemQuantity);
        requestItem.setComment(updatedItemComment);
        requestItem.setInCart(updatedItemIsInCart);

        String response = mockMvc.perform(post("/items/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ShoppingItemDTO actualItem = objectMapper.readValue(response, ShoppingItemDTO.class);
        ShoppingItemEntity actualEntity = itemRepo.findById(testItemID).get();

        assertNotNull(actualItem);
        assertEquals(testItemID, actualItem.getId());
        assertEquals(testItemName, actualItem.getItemName());
        assertEquals(updatedItemCategory, actualItem.getItemCategory());
        assertEquals(updatedItemQuantity, actualItem.getQuantity());
        assertEquals(updatedItemComment, actualItem.getComment());
        assertEquals(updatedItemIsInCart, actualItem.isInCart());

        assertEquals(1, itemRepo.count());

        assertEquals(updatedItemCategory, actualEntity.getItemCategory());
        assertEquals(updatedItemQuantity, actualEntity.getQuantity());
        assertEquals(updatedItemComment, actualEntity.getComment());
        assertEquals(updatedItemIsInCart, actualEntity.isInCart());

    }

    @Test
    void givenNoAuthToken_whenUpdateItem_thenReturnForbidden() throws Exception {
        String itemName = "testItem";
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(itemName);

        mockMvc.perform(post("/items/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenIncorrectID_whenUpdateItem_thenReturnNotFound() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        String updatedItemCategory = "someCategory";
        String updatedItemComment = "oiuaogwhldiuhuga";
        Integer updatedItemQuantity = 10;
        boolean updatedItemIsInCart = true;

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = 321L;

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName(testItemName);
        requestItem.setItemCategory(updatedItemCategory);
        requestItem.setQuantity(updatedItemQuantity);
        requestItem.setComment(updatedItemComment);
        requestItem.setInCart(updatedItemIsInCart);

        mockMvc.perform(post("/items/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    void givenNullID_whenUpdateItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        String updatedItemCategory = "someCategory";
        String updatedItemComment = "oiuaogwhldiuhuga";
        Integer updatedItemQuantity = 10;
        boolean updatedItemIsInCart = true;

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = null;

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName(testItemName);
        requestItem.setItemCategory(updatedItemCategory);
        requestItem.setQuantity(updatedItemQuantity);
        requestItem.setComment(updatedItemComment);
        requestItem.setInCart(updatedItemIsInCart);

        String response = mockMvc.perform(post("/items/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("empty"));
    }


    @Test
    void givenEmptyItemName_whenUpdateItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        String updatedItemCategory = "someCategory";
        String updatedItemComment = "oiuaogwhldiuhuga";
        Integer updatedItemQuantity = 10;
        boolean updatedItemIsInCart = true;

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = itemRepo.save(itemToSave).getId();

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName("");
        requestItem.setItemCategory(updatedItemCategory);
        requestItem.setQuantity(updatedItemQuantity);
        requestItem.setComment(updatedItemComment);
        requestItem.setInCart(updatedItemIsInCart);

        String response = mockMvc.perform(post("/items/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("name"));

    }

    @Test
    void givenBlankItemName_whenUpdateItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        String updatedItemCategory = "someCategory";
        String updatedItemComment = "oiuaogwhldiuhuga";
        Integer updatedItemQuantity = 10;
        boolean updatedItemIsInCart = true;

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = itemRepo.save(itemToSave).getId();

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName("     ");
        requestItem.setItemCategory(updatedItemCategory);
        requestItem.setQuantity(updatedItemQuantity);
        requestItem.setComment(updatedItemComment);
        requestItem.setInCart(updatedItemIsInCart);

        String response = mockMvc.perform(post("/items/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("name"));
    }

    @Test
    void givenNullItemName_whenUpdateItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        String updatedItemCategory = "someCategory";
        String updatedItemComment = "oiuaogwhldiuhuga";
        Integer updatedItemQuantity = 10;
        boolean updatedItemIsInCart = true;

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = itemRepo.save(itemToSave).getId();

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName(null);
        requestItem.setItemCategory(updatedItemCategory);
        requestItem.setQuantity(updatedItemQuantity);
        requestItem.setComment(updatedItemComment);
        requestItem.setInCart(updatedItemIsInCart);

        String response = mockMvc.perform(post("/items/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("name"));
    }

    @Test
    void givenInvalidQuantity_whenUpdateItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        String updatedItemCategory = "someCategory";
        String updatedItemComment = "oiuaogwhldiuhuga";
        Integer updatedItemQuantity = -10;
        boolean updatedItemIsInCart = true;

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = itemRepo.save(itemToSave).getId();

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName(testItemName);
        requestItem.setItemCategory(updatedItemCategory);
        requestItem.setQuantity(updatedItemQuantity);
        requestItem.setComment(updatedItemComment);
        requestItem.setInCart(updatedItemIsInCart);

        String response = mockMvc.perform(post("/items/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("quantity"));
    }

    @Test
    void givenValidItem_whenDeleteItem_thenReturnOKAndUpdateDB() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = itemRepo.save(itemToSave).getId();

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName(testItemName);

        mockMvc.perform(delete("/items/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isOk());

        assertFalse(itemRepo.findById(testItemID).isPresent());
        assertEquals(0, itemRepo.count());

    }

    @Test
    void givenNoAuthToken_whenDeleteItem_thenReturnForbidden() throws Exception {
        String itemName = "testItem";
        ShoppingItemDTO testItem = new ShoppingItemDTO();
        testItem.setItemName(itemName);

        mockMvc.perform(delete("/items/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testItem)))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenIncorrectID_whenDeleteItem_thenReturnNotFound() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = 0L;

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName(testItemName);

        mockMvc.perform(delete("/items/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isNotFound());

    }

    @Test
    void givenNullId_whenDeleteItem_thenReturnBadRequest() throws Exception {
        AuthTokenDTO authTokenDTO = createListAndLoginAndGetAuthTokenDTO();


        String testItemName = "testItem";

        ShoppingListEntity shoppingList = listRepo.findByNameIgnoreCase(authTokenDTO.getName());
        ShoppingItemEntity itemToSave = new ShoppingItemEntity();
        itemToSave.setShoppingList(shoppingList);
        itemToSave.setItemName(testItemName); // no need to set any other blank fields, since they're set by default

        Long testItemID = null;

        ShoppingItemDTO requestItem = new ShoppingItemDTO();
        requestItem.setId(testItemID);
        requestItem.setItemName(testItemName);

        mockMvc.perform(delete("/items/delete")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem))
                .header("Authorization", "Bearer " + authTokenDTO.getToken()))
                .andDo(print())
                .andExpect(status().isBadRequest());

    }

    @Test
    void whenDeleteAllItems_thenReturnOKAndRemoveDBEntries() throws Exception {
        AuthTokenDTO authToken = createListAndLoginAndGetAuthTokenDTO();
        String altListName = "altList";
        createListAndLoginAndGetAuthTokenDTO(altListName, altListName);

        String testName1 = "iauhwd";
        String testName2 = "liouiahuowd";

        ShoppingItemEntity testEntity1 = new ShoppingItemEntity();
        testEntity1.setItemName(testName1);
        testEntity1.setShoppingList(listRepo.findByNameIgnoreCase(authToken.getName()));
        ShoppingItemEntity testEntity2 = new ShoppingItemEntity();
        testEntity2.setItemName(testName2);
        testEntity2.setShoppingList(listRepo.findByNameIgnoreCase(authToken.getName()));
        ShoppingItemEntity testEntity3 = new ShoppingItemEntity();
        testEntity3.setItemName(testName1);
        testEntity3.setShoppingList(listRepo.findByNameIgnoreCase(altListName));


        itemRepo.save(testEntity1);
        itemRepo.save(testEntity2);
        itemRepo.save(testEntity3);

        assertEquals(3, itemRepo.count());

        mockMvc.perform(delete("/items/delete/all")
                .header("Authorization", "Bearer " + authToken.getToken()))
                .andDo(print())
                .andExpect(status().isOk());

        assertEquals(1, itemRepo.count());
    }

    @Test
    void givenNoAuthToken_whenDeleteAll_thenReturnForbidden() throws Exception {
        mockMvc.perform(delete("/items/delete/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void whenDeleteAllItemsInCart_thenReturnOKAndRemoveDBEntries() throws Exception {
        AuthTokenDTO authToken = createListAndLoginAndGetAuthTokenDTO();
        String altListName = "altList";
        createListAndLoginAndGetAuthTokenDTO(altListName, altListName);

        String testName1 = "iauhwd";
        String testName2 = "liouiahuowd";

        ShoppingItemEntity testEntity1 = new ShoppingItemEntity();
        testEntity1.setItemName(testName1);
        testEntity1.setShoppingList(listRepo.findByNameIgnoreCase(authToken.getName()));
        ShoppingItemEntity testEntity2 = new ShoppingItemEntity();
        testEntity2.setItemName(testName2);
        testEntity2.setInCart(true);
        testEntity2.setShoppingList(listRepo.findByNameIgnoreCase(authToken.getName()));
        ShoppingItemEntity testEntity3 = new ShoppingItemEntity();
        testEntity3.setItemName(testName1);
        testEntity3.setShoppingList(listRepo.findByNameIgnoreCase(altListName));


        itemRepo.save(testEntity1);
        itemRepo.save(testEntity2);
        itemRepo.save(testEntity3);

        assertEquals(3, itemRepo.count());

        mockMvc.perform(delete("/items/delete/incart")
                .header("Authorization", "Bearer " + authToken.getToken()))
                .andDo(print())
                .andExpect(status().isOk());

        assertEquals(2, itemRepo.count());
        assertEquals(1, itemRepo.findByShoppingList(
                listRepo.findByNameIgnoreCase(authToken.getName())
        ));
    }

    @Test
    void givenNoAuthToken_whenDeleteAllInCaart_thenReturnForbidden() throws Exception {
        mockMvc.perform(delete("/items/delete/incart")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }


    @Test
    void whenUnmarkAllItems_thenUpdateItemsInDBAndReturnFullList() throws Exception {
        AuthTokenDTO authToken = createListAndLoginAndGetAuthTokenDTO();

        String testName1 = "iauhwd";
        String testName2 = "liouiahuowd";

        ShoppingItemEntity testEntity1 = new ShoppingItemEntity();
        testEntity1.setItemName(testName1);
        testEntity1.setInCart(true);
        testEntity1.setShoppingList(listRepo.findByNameIgnoreCase(authToken.getName()));
        ShoppingItemEntity testEntity2 = new ShoppingItemEntity();
        testEntity2.setItemName(testName2);
        testEntity2.setInCart(true);
        testEntity2.setShoppingList(listRepo.findByNameIgnoreCase(authToken.getName()));
        ShoppingItemEntity testEntity3 = new ShoppingItemEntity();
        testEntity3.setItemName(testName1);
        testEntity3.setShoppingList(listRepo.findByNameIgnoreCase(authToken.getName()));


        itemRepo.save(testEntity1);
        itemRepo.save(testEntity2);
        itemRepo.save(testEntity3);


        String response = mockMvc.perform(get("/items/unmark/all")
                .header("Authorization", "Bearer " + authToken.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ShoppingItemDTO> actualItems = objectMapper.readValue(response, new TypeReference<List<ShoppingItemDTO>>() {
        });

        assertEquals(3, actualItems.size());

        actualItems = actualItems
                .stream()
                .filter(ShoppingItemDTO::isInCart)
                .collect(Collectors.toList());

        assertEquals(0, actualItems.size());
    }

    @Test
    void givenNoAuthToken_whenUnmarkAll_thenReturnForbidden() throws Exception {
        mockMvc.perform(delete("/items/unmark/all")
                .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isForbidden());
    }

    @Test
    void givenItemWithUpdatedCategory_whenAddSameItem_thenReturnUpdatedCategory() throws Exception{
        String itemName = "milk";
        String expectedOriginalCategory = "Beverages";
        String updatedCategory = "Dairy Products";

        AuthTokenDTO authToken = createListAndLoginAndGetAuthTokenDTO();

        ShoppingItemDTO firstItem = new ShoppingItemDTO();
        firstItem.setItemName(itemName);

        String firstItemResponse = mockMvc.perform(post("/items/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstItem))
                .header("Authorization", "Bearer " + authToken.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ShoppingItemDTO firstAddedItem = objectMapper.readValue(firstItemResponse,ShoppingItemDTO.class);

        assertEquals(expectedOriginalCategory,firstAddedItem.getItemCategory());

        firstAddedItem.setItemCategory(updatedCategory);

        String updateResponse = mockMvc.perform(post("/items/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstAddedItem))
                .header("Authorization", "Bearer " + authToken.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ShoppingItemDTO updateItemResponse = objectMapper.readValue(updateResponse, ShoppingItemDTO.class);

        assertEquals(updatedCategory, updateItemResponse.getItemCategory());

        String secondItemResponse = mockMvc.perform(post("/items/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(firstItem))
                .header("Authorization", "Bearer " + authToken.getToken()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        ShoppingItemDTO secondItem = objectMapper.readValue(secondItemResponse,ShoppingItemDTO.class);

        assertEquals(updatedCategory,secondItem.getItemCategory());


    }



    private AuthTokenDTO createListAndLoginAndGetAuthTokenDTO() throws Exception {
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

        return objectMapper.readValue(response, AuthTokenDTO.class);
    }

    private AuthTokenDTO createListAndLoginAndGetAuthTokenDTO(String listName, String listPassword) throws Exception {
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
}
