package com.raf.mrworldwide;

import com.raf.mrworldwide.services.clients.AmadeusClient;
import com.raf.mrworldwide.services.clients.FoursquareClient;
import com.raf.mrworldwide.services.clients.StayApiClient;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ContextConfiguration(initializers = CucumberSpringConfiguration.PostgresInitializer.class)
@ActiveProfiles("integration")
public class CucumberSpringConfiguration {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("mr-worldwide-test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
    }

    static class PostgresInitializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext ctx) {
            ctx.getEnvironment().getPropertySources().addFirst(
                    new MapPropertySource("testcontainers-postgres", Map.of(
                            "spring.datasource.url", POSTGRES.getJdbcUrl(),
                            "spring.datasource.username", POSTGRES.getUsername(),
                            "spring.datasource.password", POSTGRES.getPassword()
                    ))
            );
        }
    }

    // Mock external API clients so no real HTTP calls are made during tests
    @MockitoBean
    private AmadeusClient amadeusClient;

    @MockitoBean
    private FoursquareClient foursquareClient;

    @MockitoBean
    private StayApiClient stayApiClient;

    // Mock Spring AI to prevent OpenAI network calls at startup
    @MockitoBean
    private ChatModel chatModel;

    @MockitoBean
    private ChatClient chatClient;
}
