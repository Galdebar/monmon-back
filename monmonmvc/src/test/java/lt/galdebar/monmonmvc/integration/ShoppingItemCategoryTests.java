package lt.galdebar.monmonmvc.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import lt.galdebar.monmon.categoriesparser.services.CategoriesParserAPI;
import lt.galdebar.monmonmvc.persistence.domain.dto.LoginAttemptDTO;
import lt.galdebar.monmonmvc.persistence.domain.dto.ShoppingCategoryDTO;
import lt.galdebar.monmonmvc.persistence.repositories.UserRepo;
import lt.galdebar.monmonmvc.service.exceptions.login.UserNotFound;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = "classpath:test.properties")
@SpringBootTest
public class ShoppingItemCategoryTests {
    private static final String TEST_USER_EMAIL = "user@somemail.com";
    private static final String TEST_USER_PASS = "password";

    @TestConfiguration
    @ComponentScan(basePackages = "lt.galdebar.monmon.categoriesparser")
    @Import(CategoriesParserAPI.class)
    public class Config{
        @Bean
        public CategoriesParserAPI categoriesParserMain(){
            return new CategoriesParserAPI();
        }
    }

    @Autowired
    private CategoriesParserAPI categoriesParserAPI;

    @Autowired
    private UserRepo userRepo;

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

        categoriesParserAPI.pushCategoriesToDB();
    }

    @After
    public void tearDown() {
        userCreatorHelper.clearDB();
    }

    @Test
    public void givenValidKeywords_whenSearchCategory_thenReturnValidCategory() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String searchKeyword = "beer";
        String expectedCategory = "Beverages";
        String searchRequestResponse = createAndPostRequest(searchKeyword,authToken);

        assertNotEquals("[]",searchRequestResponse);
        assertTrue(searchRequestResponse.contains(expectedCategory));
    }

    @Test
    public void givenIncompleteKeyword_whenSearchCategory_thenReturnPossibleValidCategories() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);
        String expectedCategory = "Beverages";

        String searchKeyword1= "b";
        String searchKeyword2= "be";
        String searchKeyword3= "bev";
        String searchKeyword4= "beve";
        String searchKeyword5= "bever";
        String searchKeyword6= "bever";
        String searchKeyword7= "bevera";
        String searchKeyword8= "beverag";

        String searchResponse1 = createAndPostRequest(searchKeyword1,authToken);
        String searchResponse2 = createAndPostRequest(searchKeyword2,authToken);
        String searchResponse3 = createAndPostRequest(searchKeyword3,authToken);
        String searchResponse4 = createAndPostRequest(searchKeyword4,authToken);
        String searchResponse5 = createAndPostRequest(searchKeyword5,authToken);
        String searchResponse6 = createAndPostRequest(searchKeyword6,authToken);
        String searchResponse7 = createAndPostRequest(searchKeyword7,authToken);
        String searchResponse8 = createAndPostRequest(searchKeyword8,authToken);

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
    public void givenEmptySearch_whenSearchCategory_thenReturnEmpty() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String searchKeyword = "";
        String expectedCategory = "[]";
        String searchRequestResponse = createAndPostRequest(searchKeyword,authToken);

        assertTrue(searchRequestResponse.contains(expectedCategory));
    }

    @Test
    public void givenWeirdSearch_whenSearchCategory_thenReturnEmpty() throws Exception {
        String authToken = getAuthToken(TEST_USER_EMAIL, TEST_USER_PASS);

        String searchKeyword = ";oiahwd;oka2d";
        String expectedCategory = "[]";
        String searchRequestResponse = createAndPostRequest(searchKeyword,authToken);

        assertTrue(searchRequestResponse.contains(expectedCategory));
    }

  @Test
  public void whenGetAllCategories_thenReturnLargeArray() throws Exception {
        int expectedCategoriesCount = 42;
        String authToken = getAuthToken(TEST_USER_EMAIL,TEST_USER_PASS);
      String response =mvc.perform(get("/categorysearch/getall")
              .header("Authorization", "Bearer " + authToken))
              .andExpect(status().isOk())
              .andReturn().getResponse().getContentAsString();

      TypeFactory typeFactory = objectMapper.getTypeFactory();
      List<ShoppingCategoryDTO> shoppingCategoryList = objectMapper.readValue(response, typeFactory.constructCollectionType(List.class, ShoppingCategoryDTO.class));

      assertNotNull(shoppingCategoryList);
      assertNotEquals(0, shoppingCategoryList.size());
      assertEquals(expectedCategoriesCount,shoppingCategoryList.size());
  }

    private Map<String,String> createKeywordRequestObject(String keyword){
        Map<String, String> map = new HashMap<>();
        map.put("shoppingItemCategory", "");
        map.put("keyword", keyword);
        return map;
    }

    private String createAndPostRequest(String keyword, String authToken) throws Exception {
        Map requestObject = createKeywordRequestObject(keyword);
        return mvc.perform(post("/categorysearch")
                .header("Authorization", "Bearer " + authToken)
                .content(objectMapper.writeValueAsString(requestObject))
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
    }

    private String getAuthToken(String userName, String userPassword) throws Exception {
        Map<String, String> loginAttemptObject = new HashMap<>();
        loginAttemptObject.put("userPassword", userPassword);
        loginAttemptObject.put("userEmail", userName);
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
}
