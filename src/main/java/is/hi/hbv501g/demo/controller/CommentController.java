package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.CreateCommentRequest;
import is.hi.hbv501g.demo.entity.Comment;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.CommentService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/comments")
public class CommentController {

    private static final String SESSION_HEADER = "X-Session-Id";

    private final CommentService commentService;
    private final AuthService authService;

    public CommentController(CommentService commentService, AuthService authService) {
        this.commentService = commentService;
        this.authService = authService;
    }

    @PostMapping
    public ResponseEntity<CommentView> createComment(
            @RequestHeader(SESSION_HEADER) String sessionId, @Valid @RequestBody CreateCommentRequest request) {
        var session = authService.requireSession(sessionId);
        Comment comment = commentService.createComment(session.userId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentView.from(comment));
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentView>> listComments(@PathVariable UUID postId) {
        List<CommentView> comments = commentService.listVisible(postId).stream()
                .map(CommentView::from)
                .toList();
        return ResponseEntity.ok(comments);
    }

    public record CommentView(
            String id, String postId, String author, String body, int score, String createdAt) {

        static CommentView from(Comment comment) {
            return new CommentView(
                    comment.getId().toString(),
                    comment.getPost().getId().toString(),
                    comment.getAuthor().getUsername(),
                    comment.getBody(),
                    comment.getScore(),
                    comment.getCreatedAt().toString());
        }
    }
}
