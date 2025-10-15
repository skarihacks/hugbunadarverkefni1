package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.controller.view.PageView;
import is.hi.hbv501g.demo.controller.view.PostView;
import is.hi.hbv501g.demo.model.FeedScope;
import is.hi.hbv501g.demo.model.FeedSort;
import is.hi.hbv501g.demo.service.FeedService;
import is.hi.hbv501g.demo.service.UserService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/feed")
@Validated
public class FeedController {

    private final FeedService feedService;
    private final UserService userService;

    public FeedController(FeedService feedService, UserService userService) {
        this.feedService = feedService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<PageView<PostView>> list(
            @RequestParam(defaultValue = "GLOBAL") FeedScope scope,
            @RequestParam(defaultValue = "HOT") FeedSort sort,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int size,
            @RequestParam(required = false) String community,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) UUID userId) {

        UUID resolvedUserId = resolveUserId(scope, username, userId);
        Page<PostView> pageResult =
                feedService.list(scope, sort, page, size, community, resolvedUserId).map(PostView::from);
        PageView<PostView> body = PageView.from(pageResult);
        return ResponseEntity.ok(body);
    }

    @GetMapping("/search")
    public ResponseEntity<PageView<PostView>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "25") @Min(1) @Max(100) int size) {

        String term = q == null ? "" : q.trim();
        Page<PostView> posts = feedService.searchByTitle(term, page, size).map(PostView::from);
        return ResponseEntity.ok(PageView.from(posts));
    }

    private UUID resolveUserId(FeedScope scope, String username, UUID userId) {
        if (scope != FeedScope.USER) {
            return null;
        }
        if (userId != null) {
            return userId;
        }
        if (StringUtils.hasText(username)) {
            return userService.getByUsername(username.trim()).getId();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "username or userId is required for USER scope");
    }
}
