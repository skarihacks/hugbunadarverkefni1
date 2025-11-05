package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.CommentResponse;
import is.hi.hbv501g.demo.entity.Comment;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.service.UserService;
import java.util.Base64;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfile> profile(@PathVariable String username) {
        User user = userService.getByUsername(username.trim());
        List<Post> postEntities = userService.getRecentPosts(user.getId());
        List<Comment> commentEntities = userService.getRecentComments(user.getId());

        List<PostSummary> posts = postEntities.stream().map(PostSummary::from).toList();
        List<CommentResponse> comments = commentEntities.stream()
                .map(CommentResponse::from)
                .toList();
        int karma = postEntities.stream().mapToInt(Post::getScore).sum()
                + commentEntities.stream().mapToInt(Comment::getScore).sum();

        UserProfile body = new UserProfile(
                user.getId().toString(),
                user.getUsername(),
                user.getEmail(),
                user.getStatus().name(),
                user.getCreatedAt().toString(),
                karma,
                posts,
                comments);
        return ResponseEntity.ok(body);
    }

    public record UserProfile(
            String id,
            String username,
            String email,
            String status,
            String joinedAt,
            int karma,
            List<PostSummary> posts,
            List<CommentResponse> comments) {}

    public record PostSummary(
            String id,
            String community,
            String title,
            String body,
            String mediaBase64,
            int score,
            String createdAt) {

        static PostSummary from(Post post) {
            String media = null;
            if (post.getMediaData() != null && post.getMediaData().length > 0) {
                media = Base64.getEncoder().encodeToString(post.getMediaData());
            }
            return new PostSummary(
                    post.getId().toString(),
                    post.getCommunity().getName(),
                    post.getTitle(),
                    post.getBody(),
                    media,
                    post.getScore(),
                    post.getCreatedAt().toString());
        }
    }
}
