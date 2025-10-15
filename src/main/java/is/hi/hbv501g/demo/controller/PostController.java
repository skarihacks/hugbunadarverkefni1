package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.controller.view.PostView;
import is.hi.hbv501g.demo.dto.CreatePostRequest;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.PostService;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private static final String SESSION_HEADER = "X-Session-Id";

    private final PostService postService;
    private final AuthService authService;

    public PostController(PostService postService, AuthService authService) {
        this.postService = postService;
        this.authService = authService;
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<PostView> createPost(
            @RequestHeader(SESSION_HEADER) String sessionId, @Valid @RequestBody CreatePostRequest request) {
        var session = authService.requireSession(sessionId);
        if (request.isMedia()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use /api/posts/media for media uploads");
        }
        var post = postService.createPost(session.userId(), request, null);
        PostView body = PostView.from(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @PostMapping(path = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PostView> createMediaPost(
            @RequestHeader(SESSION_HEADER) String sessionId,
            @Valid @RequestPart("payload") CreatePostRequest request,
            @RequestPart("media") MultipartFile media) {
        var session = authService.requireSession(sessionId);
        var post = postService.createPost(session.userId(), request, media);
        PostView body = PostView.from(post);
        return ResponseEntity.status(HttpStatus.CREATED).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostView> getPost(@PathVariable UUID id) {
        var post = postService.getPost(id);
        PostView body = PostView.from(post);
        return ResponseEntity.ok(body);
    }
}
