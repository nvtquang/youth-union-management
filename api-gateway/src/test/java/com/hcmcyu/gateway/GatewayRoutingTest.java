package com.hcmcyu.gateway;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class GatewayRoutingTest {

    private static final MockWebServer AUTH_SERVICE = startMockServer();
    private static final MockWebServer MEMBER_SERVICE = startMockServer();
    private static final MockWebServer EVENT_SERVICE = startMockServer();
    private static final MockWebServer CONTENT_SERVICE = startMockServer();
    private static final MockWebServer CHAT_SERVICE = startMockServer();
    private static final MockWebServer NOTIFICATION_SERVICE = startMockServer();

    @LocalServerPort
    private int port;

    @Autowired
    private RouteLocator routeLocator;

    @DynamicPropertySource
    static void registerServiceUrls(DynamicPropertyRegistry registry) {
        registry.add("AUTH_SERVICE_URL", () -> AUTH_SERVICE.url("/").toString());
        registry.add("MEMBER_SERVICE_URL", () -> MEMBER_SERVICE.url("/").toString());
        registry.add("EVENT_SERVICE_URL", () -> EVENT_SERVICE.url("/").toString());
        registry.add("CONTENT_SERVICE_URL", () -> CONTENT_SERVICE.url("/").toString());
        registry.add("CHAT_SERVICE_URL", () -> CHAT_SERVICE.url("/").toString());
        registry.add("CHAT_SERVICE_WS_URL", () -> "ws://localhost:" + CHAT_SERVICE.getPort());
        registry.add("NOTIFICATION_SERVICE_URL", () -> NOTIFICATION_SERVICE.url("/").toString());
    }

    @AfterAll
    static void stopMockServers() throws IOException {
        AUTH_SERVICE.shutdown();
        MEMBER_SERVICE.shutdown();
        EVENT_SERVICE.shutdown();
        CONTENT_SERVICE.shutdown();
        CHAT_SERVICE.shutdown();
        NOTIFICATION_SERVICE.shutdown();
    }

    @Test
    void routesHealthRequestsToServices() throws InterruptedException {
        expectHealth(AUTH_SERVICE, "auth-service");
        expectHealth(MEMBER_SERVICE, "member-service");
        expectHealth(MEMBER_SERVICE, "member-service");
        expectHealth(EVENT_SERVICE, "event-service");
        expectHealth(CONTENT_SERVICE, "content-service");
        expectHealth(CHAT_SERVICE, "chat-service");
        expectHealth(NOTIFICATION_SERVICE, "notification-service");

        WebTestClient client = WebTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();

        assertHealth(client, "/api/auth/health", "auth-service");
        assertHealth(client, "/api/members/health", "member-service");
        assertHealth(client, "/api/organizations/health", "member-service");
        assertHealth(client, "/api/events/health", "event-service");
        assertHealth(client, "/api/posts/health", "content-service");
        assertHealth(client, "/api/chat/health", "chat-service");
        assertHealth(client, "/api/notifications/health", "notification-service");

        assertThat(AUTH_SERVICE.takeRequest().getPath()).isEqualTo("/api/health");
        assertThat(MEMBER_SERVICE.takeRequest().getPath()).isEqualTo("/api/health");
        assertThat(MEMBER_SERVICE.takeRequest().getPath()).isEqualTo("/api/health");
        assertThat(EVENT_SERVICE.takeRequest().getPath()).isEqualTo("/api/health");
        assertThat(CONTENT_SERVICE.takeRequest().getPath()).isEqualTo("/api/health");
        assertThat(CHAT_SERVICE.takeRequest().getPath()).isEqualTo("/api/health");
        assertThat(NOTIFICATION_SERVICE.takeRequest().getPath()).isEqualTo("/api/health");
    }

    @Test
    void exposesChatWebSocketRoute() {
        assertThat(routeLocator.getRoutes().map(Route::getId).collectList().block())
                .contains("chat-service-websocket");
    }

    private static MockWebServer startMockServer() {
        MockWebServer server = new MockWebServer();
        try {
            server.start();
            return server;
        } catch (IOException exception) {
            throw new IllegalStateException("Could not start mock server", exception);
        }
    }

    private static void expectHealth(MockWebServer server, String service) {
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody("{\"service\":\"" + service + "\",\"status\":\"UP\"}"));
    }

    private static void assertHealth(WebTestClient client, String path, String service) {
        client.get()
                .uri(path)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.service").isEqualTo(service)
                .jsonPath("$.status").isEqualTo("UP");
    }
}
