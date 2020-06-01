package lt.galdebar.monmon.categoriesparser;


import lt.galdebar.monmon.categoriesparser.persistence.domain.CategoryEntity;
import lt.galdebar.monmon.categoriesparser.persistence.domain.ShoppingCategoryEntity;
import lt.galdebar.monmon.categoriesparser.persistence.repositories.CategoriesRepo;
import lt.galdebar.monmon.categoriesparser.persistence.repositories.KeywordsRepo;
import lt.galdebar.monmon.categoriesparser.services.CategoriesParserAPI;
import lt.galdebar.monmon.categoriesparser.services.CategoryDTOToEntityConverter;
import lt.galdebar.monmon.categoriesparser.services.ExcelParser;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.sql.DataSource;
import java.io.File;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringJUnit4ClassRunner.class)
@TestPropertySource(locations = {"classpath:test.properties"})
@ContextConfiguration(initializers = {ExcelParserIntegrationTests.Initializer.class})
@SpringBootTest
public class ExcelParserIntegrationTests {

    @Value("${spring.datasource.username}")
    private static String username;

    @Value("${spring.datasource.password}")
    private static String password;

    @Value("${spring.datasource.dbname}")
    private static String dbName;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName(dbName)
            .withUsername(username)
            .withPassword(password);

    static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

    @Autowired
    private CategoriesParserAPI parserMain;

    @Autowired
    private KeywordsRepo keywordsRepo;

    @Autowired
    private CategoriesRepo categoriesRepo;

    @Autowired
    private ExcelParser excelParser;

    @Autowired
    private CategoryDTOToEntityConverter converter;

    @Test
    public void givenContext_whenLoaded_thenParserValid(){
        assertNotNull(parserMain);
        assertNotNull(keywordsRepo);
        assertNotNull(categoriesRepo);
        assertNotNull(excelParser);
        assertTrue(excelParser.isParserValid());
    }

    @Test
    public void whenPushToDB_thenValidDBEntries(){
        List<ShoppingCategoryEntity> expectedList = converter.convertDTOsToEntities(excelParser.getCategories());
        parserMain.pushCategoriesToDB();

        List<ShoppingCategoryEntity> actualList = categoriesRepo.findAll();
        expectedList.sort(this::compareCategoryDAO);
        actualList.sort(this::compareCategoryDAO);

        assertEquals(expectedList.size(), actualList.size());
    }

    private int compareCategoryDAO(ShoppingCategoryEntity categoryA, ShoppingCategoryEntity categoryB){
        return categoryA.getCategoryName().compareTo(categoryB.getCategoryName());
    }
}
