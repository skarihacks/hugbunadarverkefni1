package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.controller.view.PostView;
import is.hi.hbv501g.demo.service.CommunityService;
import is.hi.hbv501g.demo.service.FeedService;
import is.hi.hbv501g.demo.service.UserService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final FeedService feedService;
    private final CommunityService communityService;
    private final UserService userService;

    public SearchController(FeedService feedService, CommunityService communityService, UserService userService) {
        this.feedService = feedService;
        this.communityService = communityService;
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<SearchResults> search(@RequestParam String q) {
        String term = q == null ? "" : q.trim();
        List<PostView> posts = feedService.searchByTitle(term, 0, 10).map(PostView::from).getContent();
        List<CommunityResult> communities = communityService.search(term).stream()
                .map(CommunityResult::from)
                .collect(Collectors.toList());
        List<UserResult> users = userService.search(term).stream()
                .map(UserResult::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(new SearchResults(posts, communities, users));
    }

    public record SearchResults(
            List<PostView> posts, List<CommunityResult> communities, List<UserResult> users) {}

    public record CommunityResult(String id, String name, String description) {

        static CommunityResult from(is.hi.hbv501g.demo.entity.Community community) {
            return new CommunityResult(
                    community.getId().toString(), community.getName(), community.getDescription());
        }
    }

    public record UserResult(String id, String username, String status) {

        static UserResult from(is.hi.hbv501g.demo.entity.User user) {
            return new UserResult(user.getId().toString(), user.getUsername(), user.getStatus().name());
        }
    }
}
