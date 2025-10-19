package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.CommunityRequests;
import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.entity.Membership;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.CommunityService;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/communities")
public class CommunityController {

    private final CommunityService communityService;
    private final AuthService authService;

    public CommunityController(CommunityService communityService, AuthService authService) {
        this.communityService = communityService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<CommunityResponse> createCommunity(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId,
            @RequestBody CommunityRequests.CreateCommunityRequest request) {
        User user = authService.requireUser(sessionId);
        Community community = communityService.createCommunity(user, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommunityResponse.from(community));
    }

    @PostMapping("/join")
    public ResponseEntity<MembershipResponse> joinCommunity(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId,
            @RequestBody CommunityRequests.JoinCommunityRequest request) {
        User user = authService.requireUser(sessionId);
        Membership membership = communityService.joinCommunity(user, request.getCommunityName());
        return ResponseEntity.ok(MembershipResponse.from(membership));
    }

    @GetMapping
    public ResponseEntity<List<CommunityResponse>> listCommunities(
            @RequestParam(value = "q", required = false) String query) {
        List<CommunityResponse> communities = communityService.listCommunities(query).stream()
                .map(CommunityResponse::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(communities);
    }

    public record CommunityResponse(String id, String name, String description) {

        static CommunityResponse from(Community community) {
            return new CommunityResponse(
                    community.getId().toString(), community.getName(), community.getDescription());
        }
    }

    public record MembershipResponse(String communityId, String userId, String joinedAt) {

        static MembershipResponse from(Membership membership) {
            return new MembershipResponse(
                    membership.getCommunity().getId().toString(),
                    membership.getUser().getId().toString(),
                    membership.getJoinedAt().toString());
        }
    }
}
