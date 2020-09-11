package lt.galdebar.monmonapi.app.api;

import com.github.javafaker.Faker;
import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.app.persistence.domain.shoppingitems.ShoppingItemDTO;
import lt.galdebar.monmonapi.webscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonapi.webscraper.scheduledtasks.RunScraper;
import lt.galdebar.monmonapi.webscraper.services.scrapers.ShopNames;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;


import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:test.properties")
@ContextConfiguration(initializers = {ListTestContainersConfig.Initializer.class})
@ComponentScan(excludeFilters = {@ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = RunScraper.class)})
class ShoppingDealsControllerTest {

    @Autowired
    private ShoppingItemDealsRepo dealsRepo;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ShoppingDealsController controller;

    private static ObjectMapper objectMapper = new ObjectMapper();

    private static final Faker faker = new Faker();

    private List<ShoppingItemDealEntity> testDeals = new ArrayList<>();

    @BeforeEach
    void setup() {
        testDeals = new ArrayList<>();
        createTestItems();
    }

    @AfterEach
    void afterAll() {
        dealsRepo.deleteAll();
    }

    @Test
    void contextLoads() {
        assertNotNull(dealsRepo);
        assertNotNull(controller);
    }

    @Test
    void whenGetAllDeals_thenReturnAllDeals() throws Exception {

        int expectedItemCount = (int) dealsRepo.count();

        String response = mockMvc.perform(get("/deals/getall"))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ShoppingItemDealDTO> actualItems = objectMapper.readValue(response, new TypeReference<List<ShoppingItemDealDTO>>() {
        });

        assertNotNull(actualItems);
        assertEquals(expectedItemCount, actualItems.size());
    }

    @Test
    void givenValidRequest_whenGetDealsByShop_thenReturnListOfDeals() throws Exception {
        String shopName = ShopNames.MAXIMA.getShopName();
        int expectedItemCount = dealsRepo.findByShopTitle(shopName).size();

        String response = mockMvc.perform(get("/deals/getall/shop?shop=" + shopName))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        List<ShoppingItemDealDTO> actualItems = objectMapper.readValue(response, new TypeReference<List<ShoppingItemDealDTO>>() {
        });

        assertNotNull(actualItems);
        assertEquals(expectedItemCount, actualItems.size());

        for (ShoppingItemDealDTO dealDTO : actualItems) {
            assertTrue(dealDTO.getShopTitle().equalsIgnoreCase(shopName));
        }
    }

    @Test
    void givenInvalidShopName_whenGetDealsByShop_thenReturnBadRequest() throws Exception {
        String shopName = "someShop";

        String response = mockMvc.perform(get("/deals/getall/shop?shop=" + shopName))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("invalid shop name"));
    }

    @Test
    void givenExactUntranslatedMatch_whenGetDeal_thenReturnDeal() throws Exception {
        ShoppingItemDealEntity expectedEntity = testDeals.get(0);
        ShoppingItemDTO requestDTO = new ShoppingItemDTO();
        requestDTO.setItemName(
                expectedEntity.getUntranslatedTitle()
        );

        String response = mockMvc.perform(post("/deals/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ShoppingItemDealDTO actualItem = objectMapper.readValue(response, ShoppingItemDealDTO.class);

        assertNotNull(actualItem);
        assertEquals(expectedEntity.getUntranslatedTitle(), actualItem.getTitle());
        assertEquals(expectedEntity.getBrand(), actualItem.getBrand());
        assertEquals(expectedEntity.getShopTitle(), actualItem.getShopTitle());
        assertEquals(expectedEntity.getPrice(), actualItem.getPrice());
    }

    @Test
    void givenExactTranslatedMatch_whenGetDeal_thenReturnDeal() throws Exception {
        ShoppingItemDealEntity expectedEntity = testDeals.get(0);
        ShoppingItemDTO requestDTO = new ShoppingItemDTO();
        requestDTO.setItemName(
                expectedEntity.getTitle()
        );

        String response = mockMvc.perform(post("/deals/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ShoppingItemDealDTO actualItem = objectMapper.readValue(response, ShoppingItemDealDTO.class);

        assertNotNull(actualItem);
        assertEquals(expectedEntity.getTitle(), actualItem.getTitle());
        assertEquals(expectedEntity.getTitle(), actualItem.getTitle());
        assertEquals(expectedEntity.getBrand(), actualItem.getBrand());
        assertEquals(expectedEntity.getShopTitle(), actualItem.getShopTitle());
        assertEquals(expectedEntity.getPrice(), actualItem.getPrice());
    }

    @Test
    void givenPartialUntranslatedMatch_whenGetDeal_thenReturnDeal() throws Exception {
        ShoppingItemDealEntity expectedEntity = testDeals.get(0);
        ShoppingItemDTO requestDTO = new ShoppingItemDTO();
        requestDTO.setItemName(
                "Vynuoges"
        );

        String response = mockMvc.perform(post("/deals/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ShoppingItemDealDTO actualItem = objectMapper.readValue(response, ShoppingItemDealDTO.class);

        assertNotNull(actualItem);
        assertEquals(expectedEntity.getUntranslatedTitle(), actualItem.getTitle());
        assertEquals(expectedEntity.getBrand(), actualItem.getBrand());
        assertEquals(expectedEntity.getShopTitle(), actualItem.getShopTitle());
        assertEquals(expectedEntity.getPrice(), actualItem.getPrice());
    }

    @Test
    void givenPartialTranslatedMatch_whenGetDeal_thenReturnDeal() throws Exception {
        ShoppingItemDealEntity expectedEntity = testDeals.get(0);
        ShoppingItemDTO requestDTO = new ShoppingItemDTO();
        requestDTO.setItemName(
                "grapes"
        );

        String response = mockMvc.perform(post("/deals/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ShoppingItemDealDTO actualItem = objectMapper.readValue(response, ShoppingItemDealDTO.class);

        assertNotNull(actualItem);
        assertEquals(expectedEntity.getTitle(), actualItem.getTitle());
        assertEquals(expectedEntity.getTitle(), actualItem.getTitle());
        assertEquals(expectedEntity.getBrand(), actualItem.getBrand());
        assertEquals(expectedEntity.getShopTitle(), actualItem.getShopTitle());
        assertEquals(expectedEntity.getPrice(), actualItem.getPrice());
    }

    @Test
    void givenEmptyOrNullRequest_whenGetDeal_thenReturnBadRequest() throws Exception {
        ShoppingItemDTO nullRequest = null;
        ShoppingItemDTO nullNameRequest = new ShoppingItemDTO();
        nullNameRequest.setItemName(null);
        ShoppingItemDTO emptyNameRequest = new ShoppingItemDTO();
        emptyNameRequest.setItemName("     ");

        String nullRequestResponse = mockMvc.perform(post("/deals/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();


        String nullNameResponse = mockMvc.perform(post("/deals/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(nullNameRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(nullNameResponse.toLowerCase().contains("must have a name"));

        String emptyNameResponse = mockMvc.perform(post("/deals/find")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyNameRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(emptyNameResponse.toLowerCase().contains("must have a name"));
    }

    @Test
    void givenValidShoppingItem_whenAttachDeal_thenReturnSameItemWithDeal() throws Exception {
        ShoppingItemDealEntity expectedDeal = testDeals.get(0);
        String itemName = expectedDeal.getUntranslatedTitle();
        ShoppingItemDTO requestItem = createRandomizedShoppingItem(itemName);

        String response = mockMvc.perform(post("/deals/attach")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ShoppingItemDTO actualItem = objectMapper.readValue(response, ShoppingItemDTO.class);

        assertEquals(requestItem.getItemName(), actualItem.getItemName());
        assertEquals(requestItem.getItemCategory(), actualItem.getItemCategory());
        assertEquals(requestItem.getComment(), actualItem.getComment());
        assertEquals(requestItem.getQuantity(), actualItem.getQuantity());

        assertEquals(expectedDeal.getUntranslatedTitle(), actualItem.getDeal().getTitle());
        assertEquals(expectedDeal.getBrand(),actualItem.getDeal().getBrand());
        assertEquals(expectedDeal.getShopTitle(),actualItem.getDeal().getShopTitle());
        assertEquals(expectedDeal.getPrice(),actualItem.getDeal().getPrice(),0.001f);
    }

    @Test
    void givenValidShoppingItemWithWeirdName_whenAttachDeal_thenReturnSameItemWithNoDeal() throws Exception {
        String itemName = "oiahwdkhj";
        ShoppingItemDTO requestItem = createRandomizedShoppingItem(itemName);

        String response = mockMvc.perform(post("/deals/attach")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestItem)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        ShoppingItemDTO actualItem = objectMapper.readValue(response, ShoppingItemDTO.class);

        assertEquals(requestItem.getItemName(), actualItem.getItemName());
        assertEquals(requestItem.getItemCategory(), actualItem.getItemCategory());
        assertEquals(requestItem.getComment(), actualItem.getComment());
        assertEquals(requestItem.getQuantity(), actualItem.getQuantity());

        assertEquals("", actualItem.getDeal().getTitle());
        assertEquals("",actualItem.getDeal().getBrand());
        assertEquals("",actualItem.getDeal().getShopTitle());
        assertEquals(0f,actualItem.getDeal().getPrice(),0.001f);
    }

    @Test
    void givenValidShoppingItems_whenAttachDeals_thenReturnSameItemsWithDeals() throws Exception{
        ShoppingItemDealEntity expectedDeal1 = testDeals.get(0);
        String itemName1 = expectedDeal1.getUntranslatedTitle();
        ShoppingItemDTO requestItem1 = createRandomizedShoppingItem(itemName1);

        ShoppingItemDealEntity expectedDeal2 = testDeals.get(1);
        String itemName2 = expectedDeal1.getUntranslatedTitle();
        ShoppingItemDTO requestItem2 = createRandomizedShoppingItem(itemName1);

        List<ShoppingItemDTO> requestList = new ArrayList<>();
        requestList.add(requestItem1);
        requestList.add(requestItem2);

        String response = mockMvc.perform(post("/deals/attach/all")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<ShoppingItemDTO> actualItems = objectMapper.readValue(response, new TypeReference<List<ShoppingItemDTO>>() {
        });

        assertNotNull(actualItems);
        assertEquals(requestList.size(), actualItems.size());

        compareItems(requestItem1,actualItems.get(0));
        compareItems(requestItem2,actualItems.get(1));

        compareDeals(
                dealsRepo.findByUntranslatedTitle(requestItem1.getItemName()).get(0),
                actualItems.get(0).getDeal(),
                false
        );
        compareDeals(
                dealsRepo.findByUntranslatedTitle(requestItem2.getItemName()).get(0),
                actualItems.get(1).getDeal(),
                false
        );

    }

    @Test
    void givenValidVariousShoppingItems_whenAttachDeals_thenReturnSameItemsWithDealsWhereAppropriate() throws Exception{
        ShoppingItemDealEntity expectedDeal1 = testDeals.get(0);
        String itemName1 = expectedDeal1.getUntranslatedTitle();
        ShoppingItemDTO requestItem1 = createRandomizedShoppingItem(itemName1);

        String itemName2 = "iauhwd";
        ShoppingItemDTO requestItem2 = createRandomizedShoppingItem(itemName1);

        List<ShoppingItemDTO> requestList = new ArrayList<>();
        requestList.add(requestItem1);
        requestList.add(requestItem2);

        String response = mockMvc.perform(post("/deals/attach/all")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);

        List<ShoppingItemDTO> actualItems = objectMapper.readValue(response, new TypeReference<List<ShoppingItemDTO>>() {
        });

        assertNotNull(actualItems);
        assertEquals(requestList.size(), actualItems.size());

        compareItems(requestItem1,actualItems.get(0));
        compareItems(requestItem2,actualItems.get(1));

        compareDeals(
                dealsRepo.findByUntranslatedTitle(requestItem1.getItemName()).get(0),
                actualItems.get(0).getDeal(),
                false
        );
        compareDeals(
                dealsRepo.findByUntranslatedTitle(requestItem2.getItemName()).get(0),
                actualItems.get(1).getDeal(),
                false
        );

    }

    @Test
    void givenShoppingItemsWithInvalid_whenAttachDeals_thenReturnBadRequest() throws Exception{
        ShoppingItemDealEntity expectedDeal1 = testDeals.get(0);
        String itemName1 = expectedDeal1.getUntranslatedTitle();
        ShoppingItemDTO requestItem1 = createRandomizedShoppingItem(itemName1);

        List<ShoppingItemDTO> requestList = new ArrayList<>();
        requestList.add(requestItem1);
        requestList.add(null);

        String nullResponse = mockMvc.perform(post("/deals/attach/all")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        requestList.set(1,createRandomizedShoppingItem(null));

        String nullItemNameResponse = mockMvc.perform(post("/deals/attach/all")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        requestList.set(1,createRandomizedShoppingItem("       "));

        String emptyItemNameResponse = mockMvc.perform(post("/deals/attach/all")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestList)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

    }

    //test attach deal
    //test attach multiple deals
    //test attach if one item is invalid

    private void compareItems(ShoppingItemDTO item1, ShoppingItemDTO item2){
        assertEquals(item1.getItemName(), item2.getItemName());
        assertEquals(item1.getItemCategory(), item2.getItemCategory());
        assertEquals(item1.getComment(), item2.getComment());
        assertEquals(item1.getQuantity(), item2.getQuantity());
    }

    private void compareDeals(ShoppingItemDealEntity deal1, ShoppingItemDealDTO deal2, boolean translated ){
        if(translated){
            assertEquals(deal1.getTitle(), deal2.getTitle());
        } else assertEquals(deal1.getUntranslatedTitle(), deal2.getTitle());
        assertEquals(deal1.getBrand(),deal2.getBrand());
        assertEquals(deal1.getShopTitle(),deal2.getShopTitle());
        assertEquals(deal1.getPrice(),deal2.getPrice(),0.001f);
    }

    private ShoppingItemDTO createRandomizedShoppingItem(String itemName) {
        ShoppingItemDTO item = new ShoppingItemDTO();
        item.setItemName(itemName);
        item.setItemCategory(faker.food().dish());
        item.setQuantity(faker.number().numberBetween(1, 100));
        item.setComment(faker.chuckNorris().fact());
        return item;
    }


    private void createTestItems() {
        createAndSaveSingleDeal("Raudonosios vynuogės", "red grapes", "", ShopNames.MAXIMA.getShopName(), 1.49f);
        createAndSaveSingleDeal("Maskarponės sūris", "mascarpone cheese", "GALBANI", ShopNames.MAXIMA.getShopName(), 2.49f);
        createAndSaveSingleDeal("Sviestas", "butter", "DVARO", ShopNames.MAXIMA.getShopName(), 0.49f);
        createAndSaveSingleDeal("Pienas", "milk", "DVARO", ShopNames.IKI.getShopName(), 0.49f);
        createAndSaveSingleDeal("Pienas", "milk", "DVARO", ShopNames.RIMI.getShopName(), 0.59f);
    }

    private void createAndSaveSingleDeal(String untranslatedTitle, String translatedTitle, String brand, String shopName, float price) {
        ShoppingItemDealEntity entity = new ShoppingItemDealEntity();
        entity.setUntranslatedTitle(untranslatedTitle);
        entity.setTitle(translatedTitle);
        entity.setBrand(brand);
        entity.setShopTitle(shopName);
        entity.setPrice(price);
        dealsRepo.save(entity);
        testDeals.add(entity);
    }
}