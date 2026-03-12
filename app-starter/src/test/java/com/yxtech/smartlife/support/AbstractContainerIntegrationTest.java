package com.yxtech.smartlife.support;

import com.yxtech.smartlife.SmartLifeApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest(classes = SmartLifeApplication.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public abstract class AbstractContainerIntegrationTest {

    private static final MySQLContainer<?> MYSQL_CONTAINER = new MySQLContainer<>("mysql:8.0.36")
            .withDatabaseName("smart_life_test")
            .withUsername("test")
            .withPassword("test")
            .withCommand(
                    "--character-set-server=utf8mb4",
                    "--collation-server=utf8mb4_general_ci",
                    "--default-time-zone=+08:00"
            );

    private static final GenericContainer<?> REDIS_CONTAINER = new GenericContainer<>(DockerImageName.parse("redis:7.2.4"))
            .withExposedPorts(6379);

    static {
        System.setProperty(
                "docker.client.strategy",
                "org.testcontainers.dockerclient.EnvironmentAndSystemPropertyClientProviderStrategy"
        );
        System.setProperty(
                "docker.host",
                "unix:///Users/yangxu/Library/Containers/com.docker.docker/Data/docker.raw.sock"
        );
        System.setProperty("api.version", "1.44");
        Startables.deepStart(MYSQL_CONTAINER, REDIS_CONTAINER).join();
    }

    @DynamicPropertySource
    static void registerContainerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", MYSQL_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", MYSQL_CONTAINER::getUsername);
        registry.add("spring.datasource.password", MYSQL_CONTAINER::getPassword);
        registry.add("spring.datasource.driver-class-name", MYSQL_CONTAINER::getDriverClassName);
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
        registry.add("app.admin-auth.session-key-prefix", () -> "smart-life:test:admin:session:");
    }
}
