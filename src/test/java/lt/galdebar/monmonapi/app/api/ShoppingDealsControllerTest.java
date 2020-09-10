package lt.galdebar.monmonapi.app.api;

import lt.galdebar.monmonapi.ListTestContainersConfig;
import lt.galdebar.monmonapi.webscraper.persistence.dao.ShoppingItemDealsRepo;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealDTO;
import lt.galdebar.monmonapi.webscraper.persistence.domain.ShoppingItemDealEntity;
import lt.galdebar.monmonapi.webscraper.scheduledtasks.RunScraper;
import lt.galdebar.monmonapi.webscraper.services.scrapers.ShopNames;
import org.junit.jupiter.api.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.shaded.com.fasterxml.jackson.core.type.TypeReference;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;


import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @BeforeEach
    void setup() {
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

        for(ShoppingItemDealDTO dealDTO:actualItems){
            assertTrue(dealDTO.getShopTitle().equalsIgnoreCase(shopName));
        }
    }

    @Test
    void givenInvalidShopName_whenGetDealsByShop_thenReturnBadRequest() throws Exception{
        String shopName = "someShop";

        String response = mockMvc.perform(get("/deals/getall/shop?shop=" + shopName))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andReturn().getResponse().getErrorMessage();

        assertTrue(response.toLowerCase().contains("invalid shop name"));
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
    }
}