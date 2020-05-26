package lt.galdebar.monmonmvc.integration;

import org.junit.ClassRule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestDatasourceInitializers implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    @Value("${spring.datasource.username}")
    private static String postgresUsername;

    @Value("${spring.data.mongodb.username}")
    private static String mongoUsername;

    @Value("${spring.datasource.password}")
    private static String password;

    @Value("${spring.datasource.dbname}")
    private static String postgresDBName;

    @Value("${spring.data.mongodb.database}")
    private static String mongoDBName;

    @ClassRule
    public static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer("postgres:11.1")
            .withDatabaseName(postgresDBName)
            .withUsername(postgresUsername)
            .withPassword(password);

    @ClassRule
    public static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongodb:latest");

    @Override
    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        TestPropertyValues.of(
                "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                "spring.datasource.password=" + postgreSQLContainer.getPassword(),
                "spring.data.mongodb.hos=" + mongoDBContainer.getHost(),
                "spring.data.mongodb.port" + mongoDBContainer.getFirstMappedPort()
        ).applyTo(configurableApplicationContext.getEnvironment());
    }
}
