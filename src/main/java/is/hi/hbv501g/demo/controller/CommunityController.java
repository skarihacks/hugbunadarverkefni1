package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.CommunityRequests;
import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.CommunityService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private static final String SESSION_HEADER = "X-Session-Id";

    private final CommunityService communityService;
    private final AuthService authService;

    public CommunityController(CommunityService communityService, AuthService authService) {
        this.communityService = communityService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<CommunityView> createCommunity(
            @RequestHeader(SESSION_HEADER) String sessionId,
            @Valid @RequestBody CommunityRequests.CreateCommunityRequest request) {
        authService.requireSession(sessionId);
        Community community = communityService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommunityView.from(community));
    }

    @GetMapping("/{name}")
    public ResponseEntity<CommunityView> getCommunity(@PathVariable String name) {
        Community community = communityService.getByName(name);
        return ResponseEntity.ok(CommunityView.from(community));
    }

    @GetMapping
    public ResponseEntity<List<CommunityView>> listCommunities(@RequestParam(value = "q", required = false) String query) {
        String term = query != null ? query.trim() : null;
        List<CommunityView> communities = communityService.search(term).stream()
                .map(CommunityView::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(communities);
    }

    public record CommunityView(String id, String name, String description, String createdAt) {

        static CommunityView from(Community community) {
            return new CommunityView(
                    community.getId().toString(),
                    community.getName(),
                    community.getDescription(),
                    community.getCreatedAt().toString());
        }
    }
}
