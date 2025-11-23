package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.service.SearchService;
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

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    //get request for searching posts, communities and users
    @GetMapping
    public ResponseEntity<SearchResponse> search(@RequestParam(value = "q", required = false) String term) {
        List<PostResult> posts = searchService.searchPosts(term).stream()
                .map(PostResult::from)
                .collect(Collectors.toList());
        List<CommunityResult> communities = searchService.searchCommunities(term).stream()
                .map(CommunityResult::from)
                .collect(Collectors.toList());
        List<UserResult> users = searchService.searchUsers(term).stream()
                .map(UserResult::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(new SearchResponse(posts, communities, users));
    }

    //response body for search results
    public record SearchResponse(
            List<PostResult> posts, List<CommunityResult> communities, List<UserResult> users) {}

    //search result information for a post
    public record PostResult(String id, String communityName, String title) {

        static PostResult from(Post post) {
            return new PostResult(post.getId().toString(), post.getCommunity().getName(), post.getTitle());
        }
    }

    //search result information for a community
    public record CommunityResult(String id, String name) {

        static CommunityResult from(Community community) {
            return new CommunityResult(community.getId().toString(), community.getName());
        }
    }

    //search result information for a user
    public record UserResult(String id, String username) {

        static UserResult from(User user) {
            return new UserResult(user.getId().toString(), user.getUsername());
        }
    }
}
