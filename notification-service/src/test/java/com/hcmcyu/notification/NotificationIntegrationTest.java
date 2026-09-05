package com.hcmcyu.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmcyu.notification.entity.Notification;
import com.hcmcyu.notification.entity.NotificationType;
import com.hcmcyu.notification.entity.ReferenceType;
import com.hcmcyu.notification.entity.UserNotification;
import com.hcmcyu.notification.repository.NotificationRepository;
import com.hcmcyu.notification.repository.UserNotificationRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.crypto.SecretKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hcmcyu_notification_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "auth.jwt.secret=test-hcmcyu-notification-service-secret-key-change-me-please-32",
        "notification.internal.secret=test-internal-secret"
})
class NotificationIntegrationTest {

    private static final String JWT_SECRET = "test-hcmcyu-notification-service-secret-key-change-me-please-32";
    private static final String INTERNAL_SECRET = "test-internal-secret";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserNotificationRepository userNotificationRepository;

    @BeforeEach
    void setUp() {
        userNotificationRepository.deleteAll();
        notificationRepository.deleteAll();
    }

    @Test
    void internalApiCanCreateNotificationForRecipients() throws Exception {
        mockMvc.perform(post("/internal/notifications")
                        .header("X-Internal-Secret", INTERNAL_SECRET)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "notificationType", "EVENT_NEW",
                                "title", "Su kien moi",
                                "content", "Moi doan vien dang ky tham gia",
                                "referenceType", "EVENT",
                                "referenceId", "event-1",
                                "recipientMemberIds", List.of("member-1", "member-2", "member-1")
                        ))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.notificationId").isNotEmpty())
                .andExpect(jsonPath("$.recipientCount").value(2));

        assertThat(notificationRepository.findAll()).hasSize(1);
        assertThat(userNotificationRepository.findAll()).hasSize(2);
    }

    @Test
    void invalidInternalSecretIsRejected() throws Exception {
        mockMvc.perform(post("/internal/notifications")
                        .header("X-Internal-Secret", "wrong-secret")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of(
                                "notificationType", "SYSTEM",
                                "title", "Thong bao",
                                "content", "Noi dung",
                                "recipientMemberIds", List.of("member-1")
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("INVALID_INTERNAL_SECRET"));
    }

    @Test
    void memberOnlyReadsOwnNotificationsWithPagination() throws Exception {
        createNotification("member-1", "Tin cua member 1");
        createNotification("member-1", "Tin khac cua member 1");
        createNotification("member-2", "Tin cua member 2");

        mockMvc.perform(get("/api/notifications?page=0&size=10&sort=createdAt,desc")
                        .header(HttpHeaders.AUTHORIZATION, bearer("member-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value(org.hamcrest.Matchers.containsString("member 1")))
                .andExpect(jsonPath("$.content[1].title").value(org.hamcrest.Matchers.containsString("member 1")));
    }

    @Test
    void unreadCountAndMarkReadWorkForCurrentMember() throws Exception {
        UserNotification first = createNotification("member-1", "Tin 1");
        createNotification("member-1", "Tin 2");

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header(HttpHeaders.AUTHORIZATION, bearer("member-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(2));

        mockMvc.perform(put("/api/notifications/{id}/read", first.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer("member-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true));

        mockMvc.perform(get("/api/notifications/unread-count")
                        .header(HttpHeaders.AUTHORIZATION, bearer("member-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    void memberCannotMarkAnotherMembersNotificationAsRead() throws Exception {
        UserNotification otherMemberNotification = createNotification("member-2", "Tin cua member 2");

        mockMvc.perform(put("/api/notifications/{id}/read", otherMemberNotification.getId())
                        .header(HttpHeaders.AUTHORIZATION, bearer("member-1")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));

        assertThat(userNotificationRepository.findById(otherMemberNotification.getId()).orElseThrow().getReadAt())
                .isNull();
    }

    @Test
    void readAllOnlyMarksCurrentMembersNotifications() throws Exception {
        createNotification("member-1", "Tin 1");
        createNotification("member-1", "Tin 2");
        createNotification("member-2", "Tin member 2");

        mockMvc.perform(put("/api/notifications/read-all")
                        .header(HttpHeaders.AUTHORIZATION, bearer("member-1")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.count").value(0));

        assertThat(userNotificationRepository.countByMemberIdAndReadAtIsNull("member-1")).isZero();
        assertThat(userNotificationRepository.countByMemberIdAndReadAtIsNull("member-2")).isEqualTo(1);
    }

    @Test
    void protectedEndpointWithoutTokenReturnsUnauthorized() throws Exception {
        mockMvc.perform(get("/api/notifications"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private UserNotification createNotification(String memberId, String title) {
        Notification notification = new Notification();
        notification.setNotificationType(NotificationType.POST_NEW);
        notification.setTitle(title);
        notification.setContent("Noi dung thong bao");
        notification.setReferenceType(ReferenceType.POST);
        notification.setReferenceId("post-1");

        UserNotification userNotification = new UserNotification();
        userNotification.setMemberId(memberId);
        notification.addRecipient(userNotification);
        notificationRepository.saveAndFlush(notification);
        return userNotification;
    }

    private String bearer(String memberId) {
        return "Bearer " + token(memberId);
    }

    private String token(String memberId) {
        SecretKey secretKey = Keys.hmacShaKeyFor(JWT_SECRET.getBytes(StandardCharsets.UTF_8));
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(memberId + "-user")
                .claim("userId", memberId + "-user")
                .claim("memberId", memberId)
                .claim("username", memberId)
                .claim("email", memberId + "@example.com")
                .claim("role", "MEMBER")
                .claim("organizationId", "ward-thuong-cat")
                .claim("tdpId", "tdp-1")
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(900)))
                .signWith(secretKey)
                .compact();
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }
}
