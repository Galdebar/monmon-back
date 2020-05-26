package lt.galdebar.monmonmvc.integration;

import org.junit.ClassRule;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;

@TestConfiguration
public class TestContainersConfig {
    private static String postgresUsername = "postgres";
    private static String password = "letmein";
    private static String postgresDBName = "MonMonCategories";

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName(postgresDBName)
            .withUsername(postgresUsername)
            .withPassword(password);

    @ClassRule
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer();

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            postgreSQLContainer.start();
            mongoDBContainer.start();
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgresUsername,
                    "spring.datasource.password=" + password,
                    "spring.data.mongodb.host=" + mongoDBContainer.getHost(),
                    "spring.data.mongodb.port=" + mongoDBContainer.getMappedPort(27017)
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }
}
