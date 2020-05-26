package lt.galdebar.monmonmvc.integration;

import org.jetbrains.annotations.NotNull;
import org.testcontainers.containers.GenericContainer;

public class MongoDBContainer extends GenericContainer<MongoDBContainer> {

    public static final int MONGODB_PORT = 27017;
    public static final String DEFAULT_IMAGE_AND_TAG = "mongo:4.0";

    public MongoDBContainer() {
        this(DEFAULT_IMAGE_AND_TAG);
    }

    public MongoDBContainer(@NotNull String image) {
        super(image);
        addExposedPort(MONGODB_PORT);
        withEnv("NAME", "monmonmongo");
        withEnv("MONGO_INITDB_ROOT_USERNAME", "mongo");
        withEnv("MONGO_INITDB_ROOT_PASSWORD", "letmein");
    }

    @NotNull
    public Integer getPort() {
        return getMappedPort(MONGODB_PORT);
    }
}
