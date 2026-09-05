package com.hcmcyu.content;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hcmcyu.content.entity.Post;
import com.hcmcyu.content.entity.PostImage;
import com.hcmcyu.content.entity.PostStatus;
import com.hcmcyu.content.entity.PostType;
import com.hcmcyu.content.repository.PostImageRepository;
import com.hcmcyu.content.repository.PostRepository;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:hcmcyu_content_test;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.hibernate.ddl-auto=validate",
        "storage.local.post-image-path=target/test-post-images",
        "storage.post-image.max-size-bytes=16"
})
class PostIntegrationTest {

    private static final String WARD_ID = "ward-thuong-cat";
    private static final String TDP_1_ID = "tdp-1";
    private static final String TDP_2_ID = "tdp-2";
    private static final Path POST_IMAGE_STORAGE_PATH = Path.of("target/test-post-images");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostImageRepository postImageRepository;

    private Post wardPost;
    private Post tdp1Post;
    private Post tdp2Post;
    private Post draftPost;

    @BeforeEach
    void setUp() throws Exception {
        cleanStorage();
        postImageRepository.deleteAll();
        postRepository.deleteAll();

        wardPost = savePost("Thong bao phuong", PostType.ANNOUNCEMENT, WARD_ID, PostStatus.PUBLISHED, "ward-secretary");
        tdp1Post = savePost("Bao cao TDP 1", PostType.ACTIVITY_REPORT, TDP_1_ID, PostStatus.PUBLISHED, "tdp1-secretary");
        tdp2Post = savePost("Bao cao TDP 2", PostType.ACTIVITY_REPORT, TDP_2_ID, PostStatus.PUBLISHED, "tdp2-secretary");
        draftPost = savePost("Ban nhap TDP 1", PostType.ACTIVITY_REPORT, TDP_1_ID, PostStatus.DRAFT, "tdp1-secretary");
    }

    @Test
    void wardSecretaryCanCrudPostsAcrossWard() throws Exception {
        MvcResult result = mockMvc.perform(withWardSecretary(post("/api/posts"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(postPayload("Tin phuong", PostType.NEWS, TDP_2_ID, PostStatus.PUBLISHED))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Tin phuong"))
                .andExpect(jsonPath("$.organizationId").value(TDP_2_ID))
                .andReturn();

        String postId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(withWardSecretary(put("/api/posts/{id}", postId))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(postPayload("Tin phuong updated", PostType.OTHER, TDP_1_ID, PostStatus.ARCHIVED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Tin phuong updated"))
                .andExpect(jsonPath("$.organizationId").value(TDP_1_ID));

        mockMvc.perform(withWardSecretary(delete("/api/posts/{id}", postId)))
                .andExpect(status().isNoContent());

        assertThat(postRepository.findById(postId)).isEmpty();
    }

    @Test
    void tdpSecretaryCanManageActivityReportInOwnTdpOnly() throws Exception {
        MvcResult result = mockMvc.perform(withTdpSecretary(post("/api/posts"), TDP_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(postPayload("Bao cao moi TDP 1", PostType.ACTIVITY_REPORT, TDP_1_ID, PostStatus.PUBLISHED))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.organizationId").value(TDP_1_ID))
                .andReturn();

        String postId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();

        mockMvc.perform(withTdpSecretary(put("/api/posts/{id}", postId), TDP_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(postPayload("Bao cao moi TDP 1 updated", PostType.ACTIVITY_REPORT, TDP_1_ID, PostStatus.PUBLISHED))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Bao cao moi TDP 1 updated"));
    }

    @Test
    void tdpSecretaryCannotManageAnotherTdpOrNonReportPost() throws Exception {
        mockMvc.perform(withTdpSecretary(post("/api/posts"), TDP_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(postPayload("Tin sai quyen", PostType.NEWS, TDP_1_ID, PostStatus.PUBLISHED))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));

        mockMvc.perform(withTdpSecretary(post("/api/posts"), TDP_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(postPayload("Sai TDP", PostType.ACTIVITY_REPORT, TDP_2_ID, PostStatus.PUBLISHED))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));

        mockMvc.perform(withTdpSecretary(put("/api/posts/{id}", tdp2Post.getId()), TDP_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(postPayload("Blocked", PostType.ACTIVITY_REPORT, TDP_2_ID, PostStatus.PUBLISHED))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));
    }

    @Test
    void memberCanOnlyViewPublishedPostsInScope() throws Exception {
        mockMvc.perform(withMember(get("/api/posts"), "member-1", TDP_1_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(wardPost.getId())).exists())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(tdp1Post.getId())).exists())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(tdp2Post.getId())).doesNotExist())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(draftPost.getId())).doesNotExist());

        mockMvc.perform(withMember(get("/api/posts/{id}", draftPost.getId()), "member-1", TDP_1_ID))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));

        mockMvc.perform(withMember(post("/api/posts"), "member-1", TDP_1_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(postPayload("Member blocked", PostType.ACTIVITY_REPORT, TDP_1_ID, PostStatus.PUBLISHED))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("OUT_OF_SCOPE"));
    }

    @Test
    void supportsOrganizationTypeDatePaginationFilters() throws Exception {
        mockMvc.perform(withWardSecretary(get("/api/posts")
                        .param("organization", TDP_1_ID)
                        .param("type", "ACTIVITY_REPORT")
                        .param("date", LocalDate.now().toString())
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,asc")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(tdp1Post.getId())).exists())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(draftPost.getId())).exists())
                .andExpect(jsonPath("$.content[?(@.id == '%s')]".formatted(tdp2Post.getId())).doesNotExist());
    }

    @Test
    void officerCanUploadMultipleImagesAndDeleteImage() throws Exception {
        MockMultipartFile image1 = new MockMultipartFile("files", "report-1.png", "image/png", new byte[] {1, 2});
        MockMultipartFile image2 = new MockMultipartFile("files", "report-2.webp", "image/webp", new byte[] {3, 4});

        mockMvc.perform(withTdpSecretary(multipart("/api/posts/{id}/images", tdp1Post.getId())
                        .file(image1)
                        .file(image2), TDP_1_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.images.length()").value(2))
                .andExpect(jsonPath("$.images[0].id").isNotEmpty())
                .andExpect(jsonPath("$.images[0].imageUrl").value(org.hamcrest.Matchers.startsWith("/uploads/post-images/")));

        var images = postImageRepository.findAll();
        assertThat(images).hasSize(2);
        PostImage firstImage = images.getFirst();
        String firstUrl = firstImage.getImageUrl();
        String storedFilename = firstUrl.substring("/uploads/post-images/".length());
        assertThat(storedFilename).doesNotContain("report-1.png");
        assertThat(Files.exists(POST_IMAGE_STORAGE_PATH.resolve(storedFilename))).isTrue();

        String imageId = firstImage.getId();
        mockMvc.perform(withTdpSecretary(delete("/api/posts/{id}/images/{imageId}", tdp1Post.getId(), imageId), TDP_1_ID))
                .andExpect(status().isNoContent());

        assertThat(postImageRepository.findById(imageId)).isEmpty();
        assertThat(Files.exists(POST_IMAGE_STORAGE_PATH.resolve(storedFilename))).isFalse();
    }

    @Test
    void uploadImageValidatesFileTypeAndSize() throws Exception {
        MockMultipartFile wrongType = new MockMultipartFile("files", "note.txt", "text/plain", new byte[] {1});

        mockMvc.perform(withTdpSecretary(multipart("/api/posts/{id}/images", tdp1Post.getId())
                        .file(wrongType), TDP_1_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_POST_IMAGE_FILE"));

        MockMultipartFile tooLarge = new MockMultipartFile("files", "large.png", "image/png", new byte[17]);

        mockMvc.perform(withTdpSecretary(multipart("/api/posts/{id}/images", tdp1Post.getId())
                        .file(tooLarge), TDP_1_ID))
                .andExpect(status().isPayloadTooLarge())
                .andExpect(jsonPath("$.code").value("POST_IMAGE_TOO_LARGE"));
    }

    private Post savePost(
            String title,
            PostType type,
            String organizationId,
            PostStatus status,
            String authorId
    ) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(title + " content");
        post.setType(type);
        post.setOrganizationId(organizationId);
        post.setAuthorId(authorId);
        post.setStatus(status);
        return postRepository.save(post);
    }

    private Map<String, Object> postPayload(
            String title,
            PostType type,
            String organizationId,
            PostStatus status
    ) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("title", title);
        payload.put("content", title + " content");
        payload.put("type", type.name());
        payload.put("organizationId", organizationId);
        payload.put("status", status.name());
        return payload;
    }

    private MockHttpServletRequestBuilder withWardSecretary(MockHttpServletRequestBuilder request) {
        return request
                .header("X-User-Id", "ward-secretary-user")
                .header("X-Member-Id", "ward-secretary-member")
                .header("X-User-Role", "WARD_SECRETARY")
                .header("X-Organization-Id", WARD_ID);
    }

    private MockHttpServletRequestBuilder withTdpSecretary(MockHttpServletRequestBuilder request, String tdpId) {
        return request
                .header("X-User-Id", "tdp-secretary-user")
                .header("X-Member-Id", "tdp-secretary-member")
                .header("X-User-Role", "TDP_SECRETARY")
                .header("X-Organization-Id", WARD_ID)
                .header("X-Tdp-Id", tdpId);
    }

    private MockHttpServletRequestBuilder withMember(
            MockHttpServletRequestBuilder request,
            String memberId,
            String tdpId
    ) {
        return request
                .header("X-User-Id", memberId + "-user")
                .header("X-Member-Id", memberId)
                .header("X-User-Role", "MEMBER")
                .header("X-Organization-Id", WARD_ID)
                .header("X-Tdp-Id", tdpId);
    }

    private String json(Object value) throws Exception {
        return objectMapper.writeValueAsString(value);
    }

    private void cleanStorage() throws Exception {
        if (!Files.exists(POST_IMAGE_STORAGE_PATH)) {
            return;
        }
        try (var files = Files.walk(POST_IMAGE_STORAGE_PATH)) {
            files.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (Exception exception) {
                            throw new IllegalStateException(exception);
                        }
                    });
        }
    }
}
