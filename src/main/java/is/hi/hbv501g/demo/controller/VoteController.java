package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.VoteRequest;
import is.hi.hbv501g.demo.entity.Vote;
import is.hi.hbv501g.demo.entity.VoteTargetType;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.VoteService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private final VoteService voteService;
    private final AuthService authService;

    public VoteController(VoteService voteService, AuthService authService) {
        this.voteService = voteService;
        this.authService = authService;
    }

    //post request for recording a vote
    @PostMapping
    public ResponseEntity<VoteView> recordVote(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId, @RequestBody VoteRequest request) {
        UUID userId = authService.requireUser(sessionId).getId();
        Vote vote = voteService.recordVote(userId, request);
        return ResponseEntity.ok(VoteView.from(vote));
    }

    //get request for retrieving a vote by id
    @GetMapping("/{id}")
    public ResponseEntity<VoteView> getVote(@PathVariable UUID id) {
        Vote vote = voteService.getVote(id);
        return ResponseEntity.ok(VoteView.from(vote));
    }

    //get request for retrieving votes for a post
    @GetMapping("/posts/{postId}")
    public ResponseEntity<List<VoteView>> listPostVotes(@PathVariable UUID postId) {
        List<VoteView> votes = voteService.getVotesByTarget(postId, VoteTargetType.POST).stream()
                .map(VoteView::from)
                .toList();
        return ResponseEntity.ok(votes);
    }

    //response body for vote information
    public record VoteView(String id, String targetType, String targetId, String value) {

        static VoteView from(Vote vote) {
            return new VoteView(
                    vote.getId().toString(),
                    vote.getTargetType().name(),
                    vote.getTargetId().toString(),
                    vote.getValue().name());
        }
    }
}
