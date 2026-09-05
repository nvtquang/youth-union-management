package com.hcmcyu.content.controller;

import com.hcmcyu.content.dto.PostRequest;
import com.hcmcyu.content.dto.PostResponse;
import com.hcmcyu.content.entity.PostType;
import com.hcmcyu.content.security.CurrentUser;
import com.hcmcyu.content.service.PostService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public Page<PostResponse> findAll(
            @RequestParam(required = false, name = "organization") String organizationId,
            @RequestParam(required = false, name = "type") PostType type,
            @RequestParam(required = false, name = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            Pageable pageable,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return postService.findAll(organizationId, type, date, pageable, currentUser);
    }

    @GetMapping("/{id}")
    public PostResponse findById(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return postService.findById(id, currentUser);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return postService.create(request, currentUser);
    }

    @PutMapping("/{id}")
    public PostResponse update(
            @PathVariable("id") String id,
            @Valid @RequestBody PostRequest request,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return postService.update(id, request, currentUser);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable("id") String id,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        postService.delete(id, currentUser);
    }

    @PostMapping("/{id}/images")
    public PostResponse uploadImages(
            @PathVariable("id") String id,
            @RequestParam("files") List<MultipartFile> files,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return postService.uploadImages(id, files, currentUser);
    }

    @DeleteMapping("/{id}/images/{imageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteImage(
            @PathVariable("id") String id,
            @PathVariable("imageId") String imageId,
            @AuthenticationPrincipal CurrentUser currentUser
    ) {
        postService.deleteImage(id, imageId, currentUser);
    }
}
