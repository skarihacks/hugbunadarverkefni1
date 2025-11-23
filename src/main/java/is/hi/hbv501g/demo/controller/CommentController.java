package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.CommentResponse;
import is.hi.hbv501g.demo.dto.CreateCommentRequest;
import is.hi.hbv501g.demo.dto.UpdateCommentRequest;
import is.hi.hbv501g.demo.entity.Comment;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.service.AuthService;
import is.hi.hbv501g.demo.service.CommentService;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CommentController {

    private final CommentService commentService;
    private final AuthService authService;

    public CommentController(CommentService commentService, AuthService authService) {
        this.commentService = commentService;
        this.authService = authService;
    }


    //post request for creating a new comment
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId,
            @PathVariable UUID postId,
            @RequestBody CreateCommentRequest request) {
        User author = authService.requireUser(sessionId);
        Comment comment = commentService.createComment(author, postId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CommentResponse.from(comment));
    }

    //put request for updating a comment
    @PutMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> updateComment(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId,
            @PathVariable UUID id,
            @RequestBody UpdateCommentRequest request) {
        UUID userId = authService.requireUser(sessionId).getId();
        Comment updated = commentService.updateComment(id, userId, request);
        return ResponseEntity.ok(CommentResponse.from(updated));
    }

    //delete request for deleting a comment
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<Void> deleteComment(
            @RequestHeader(AuthController.SESSION_HEADER) String sessionId, @PathVariable UUID id) {
        UUID userId = authService.requireUser(sessionId).getId();
        commentService.deleteComment(id, userId);
        return ResponseEntity.noContent().build();
    }
    
    //get request for retrieving a comment by id
    @GetMapping("/comments/{id}")
    public ResponseEntity<CommentResponse> getComment(@PathVariable UUID id) {
        Comment comment = commentService.getComment(id);
        return ResponseEntity.ok(CommentResponse.from(comment));
    }

    //get request for retrieving all comments
    @GetMapping("/comments")
    public ResponseEntity<List<CommentResponse>> getAllComments() {
        List<CommentResponse> response = commentService.getAllComments().stream()
                .map(CommentResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
    //get request for retrieving comments by post id
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<CommentResponse>> getCommentsByPost(@PathVariable UUID postId) {
        List<CommentResponse> response = commentService.getCommentsByPostId(postId).stream()
                .map(CommentResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }
}
