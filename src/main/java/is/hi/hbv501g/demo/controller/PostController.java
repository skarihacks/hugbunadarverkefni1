package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.CreatePostRequest;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.PostService;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @PostMapping
    public ResponseEntity<PostResponse> createPost(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId,
            @RequestBody CreatePostRequest request) {
        User author = authService.requireUser(sessionId);
        Post post = postService.createPost(author, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(PostResponse.from(post));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(@PathVariable UUID id) {
        Post post = postService.getPost(id);
        return ResponseEntity.ok(PostResponse.from(post));
    }

    public record PostResponse(String id, String communityId, String communityName, String title, String body, String createdAt) {

        static PostResponse from(Post post) {
            return new PostResponse(
                    post.getId().toString(),
                    post.getCommunity().getId().toString(),
                    post.getCommunity().getName(),
                    post.getTitle(),
                    post.getBody(),
                    post.getCreatedAt().toString());
        }
    }
}
