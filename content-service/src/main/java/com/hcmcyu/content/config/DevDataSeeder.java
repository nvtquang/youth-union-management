package com.hcmcyu.content.config;

import com.hcmcyu.content.entity.Post;
import com.hcmcyu.content.entity.PostImage;
import com.hcmcyu.content.entity.PostStatus;
import com.hcmcyu.content.entity.PostType;
import com.hcmcyu.content.repository.PostRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class DevDataSeeder implements CommandLineRunner {

    private final PostRepository postRepository;

    public DevDataSeeder(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    @Override
    public void run(String... args) {
        if (postRepository.count() > 0) {
            return;
        }

        seedPost(
                "Thong bao lich sinh hoat Doan phuong",
                "Lich sinh hoat va trien khai cong viec thang nay.",
                PostType.ANNOUNCEMENT,
                "ward-thuong-cat",
                "ward-secretary-member",
                "dev-ward-announcement.png"
        );
        seedPost(
                "Tin moi tu Doan phuong Thuong Cat",
                "Cap nhat cac hoat dong noi bat cua Doan phuong.",
                PostType.NEWS,
                "ward-thuong-cat",
                "ward-deputy-member",
                "dev-ward-news.png"
        );

        for (int tdp = 1; tdp <= 5; tdp++) {
            seedPost(
                    "Bao cao hoat dong TDP " + tdp,
                    "Bao cao anh hoat dong tinh nguyen cua chi doan TDP " + tdp + ".",
                    PostType.ACTIVITY_REPORT,
                    "tdp-" + tdp,
                    "tdp-" + tdp + "-secretary-member",
                    "dev-tdp-" + tdp + "-report.png"
            );
            seedPost(
                    "Thong tin sinh hoat TDP " + tdp,
                    "Noi dung sinh hoat chi doan TDP " + tdp + ".",
                    PostType.OTHER,
                    "tdp-" + tdp,
                    "tdp-" + tdp + "-deputy-member",
                    "dev-tdp-" + tdp + "-activity.webp"
            );
        }
    }

    private void seedPost(
            String title,
            String content,
            PostType type,
            String organizationId,
            String authorId,
            String imageName
    ) {
        Post post = new Post();
        post.setTitle(title);
        post.setContent(content);
        post.setType(type);
        post.setOrganizationId(organizationId);
        post.setAuthorId(authorId);
        post.setStatus(PostStatus.PUBLISHED);

        PostImage image = new PostImage();
        image.setImageUrl("/uploads/post-images/" + imageName);
        post.addImage(image);
        postRepository.save(post);
    }
}
