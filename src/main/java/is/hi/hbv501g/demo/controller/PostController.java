package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.CreatePostRequest;
import is.hi.hbv501g.demo.dto.UpdatePostRequest;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.PostService;
import java.util.Base64;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final AuthService authService;

    public PostController(PostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    //post request for creating a new post
    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId,
            @RequestBody CreatePostRequest request) {
        User author = authService.requireUser(sessionId);
        Post post = postService.createPost(author, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    //put request for updating a post
    @PutMapping("/{id}")
    public ResponseEntity<PostResponse> updatePost(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId,
            @PathVariable UUID id,
            @RequestBody UpdatePostRequest request) {
        UUID userId = authService.requireUser(sessionId).getId();
        Post updated = postService.updatePost(id, userId, request);
        return ResponseEntity.ok(PostResponse.from(updated));
    }

    //delete request for deleting a post
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId, @PathVariable UUID id) {
        UUID userId = authService.requireUser(sessionId).getId();
        postService.deletePost(id, userId);
        return ResponseEntity.noContent().build();
    }

    //get request for retrieving a post by id
    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID id) {
        Post post = postService.getPost(id);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    //response body for post information
    public record PostResponse(
            String id,
            String communityId,
            String communityName,
            String title,
            String body,
            String mediaBase64,
            String type,
            int score,
            String createdAt) {

        static PostResponse from(Post post) {
            String media = null;
            if (post.getMediaData() != null && post.getMediaData().length > 0) {
                media = Base64.getEncoder().encodeToString(post.getMediaData());
            }
            return new PostResponse(
                    post.getId().toString(),
                    post.getCommunity().getId().toString(),
                    post.getCommunity().getName(),
                    post.getTitle(),
                    post.getBody(),
                    media,
                    post.getType().name(),
                    post.getScore(),
                    post.getCreatedAt().toString());
        }
    }
}
