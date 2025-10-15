package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.VoteRequest;
import is.hi.hbv501g.demo.entity.Vote;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.VoteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/votes")
public class VoteController {

    private static final String SESSION_HEADER = "X-Session-Id";

    private final VoteService voteService;
    private final AuthService authService;

    public VoteController(VoteService voteService, AuthService authService) {
        this.voteService = voteService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<VoteView> recordVote(
            @RequestHeader(SESSION_HEADER) String sessionId, @Valid @RequestBody VoteRequest request) {
        var session = authService.requireSession(sessionId);
        Vote vote = voteService.recordVote(session.userId(), request);
        return ResponseEntity.ok(VoteView.from(vote));
    }

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
