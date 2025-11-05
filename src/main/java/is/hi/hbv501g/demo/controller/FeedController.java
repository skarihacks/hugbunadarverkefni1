package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.FeedService;
import java.util.Base64;
import java.util.List;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feed")
public class FeedController {

    private final FeedService feedService;
    private final AuthService authService;

    public FeedController(FeedService feedService, AuthService authService) {
        this.feedService = feedService;
        this.authService = authService;
    }

    @GetMapping
    public ResponseEntity<List<PostView>> feed(
            @RequestParam(value = "sort", required = false) String sort,
            @RequestParam(value = "community", required = false) String community) {
        List<PostView> posts = feedService.listFeed(sort, community).stream()
                .map(PostView::from)
                .toList();
        return ResponseEntity.ok(posts);
    }

    @GetMapping("/memberships")
    public ResponseEntity<List<PostView>> membershipFeed(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId,
            @RequestParam(value = "sort", required = false) String sort) {
        UUID userId = authService.requireUser(sessionId).getId();
        List<PostView> posts = feedService.listMemberFeed(sort, userId).stream()
                .map(PostView::from)
                .toList();
        return ResponseEntity.ok(posts);
    }

    public record PostView(
            String id,
            String community,
            String title,
            String body,
            String mediaBase64,
            String type,
            int score,
            String createdAt) {

        static PostView from(Post post) {
            String media = null;
            if (post.getMediaData() != null && post.getMediaData().length > 0) {
                media = Base64.getEncoder().encodeToString(post.getMediaData());
            }
            return new PostView(
                    post.getId().toString(),
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
