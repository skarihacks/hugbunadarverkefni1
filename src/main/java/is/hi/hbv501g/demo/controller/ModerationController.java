package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.ModerationService;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/moderation")
public class ModerationController {

    private final ModerationService moderationService;
    private final AuthService authService;

    public ModerationController(ModerationService moderationService, AuthService authService) {
        this.moderationService = moderationService;
        this.authService = authService;
    }

    @PostMapping("/posts/{postId}/remove")
    public ResponseEntity<Void> removePost(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId, @PathVariable UUID postId) {
        UUID userId = authService.requireUser(sessionId).getId();
        moderationService.removePost(userId, postId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/comments/{commentId}/remove")
    public ResponseEntity<Void> removeComment(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId, @PathVariable UUID commentId) {
        UUID userId = authService.requireUser(sessionId).getId();
        moderationService.removeComment(userId, commentId);
        return ResponseEntity.noContent().build();
    }
}
