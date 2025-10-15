package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.controller.view.PageView;
import is.hi.hbv501g.demo.controller.view.PostView;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.model.FeedScope;
import is.hi.hbv501g.demo.model.FeedSort;
import is.hi.hbv501g.demo.service.FeedService;
import is.hi.hbv501g.demo.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    private final UserService userService;
    private final FeedService feedService;

    public UserController(UserService userService, FeedService feedService) {
        this.userService = userService;
        this.feedService = feedService;
    }

    @GetMapping("/{username}")
    public ResponseEntity<UserProfileView> getProfile(
            @PathVariable String username,
            @RequestParam(defaultValue = "NEW") FeedSort sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size) {

        String safeUsername = username.trim();
        User user = userService.getByUsername(safeUsername);
        var postsPage = feedService
                .list(FeedScope.USER, sort, page, size, null, user.getId())
                .map(PostView::from);
        UserProfileView body = new UserProfileView(UserSummary.from(user), PageView.from(postsPage));
        return ResponseEntity.ok(body);
    }

    public record UserProfileView(UserSummary user, PageView<PostView> posts) {}

    public record UserSummary(String id, String username, String email, String status, int karma) {

        static UserSummary from(User user) {
            return new UserSummary(
                    user.getId().toString(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getStatus().name(),
                    user.getKarma());
        }
    }
}
