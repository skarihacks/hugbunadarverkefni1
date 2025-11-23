package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.CreateCommentRequest;
import is.hi.hbv501g.demo.dto.UpdateCommentRequest;
import is.hi.hbv501g.demo.entity.Comment;
import is.hi.hbv501g.demo.entity.CommentState;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.repository.CommentRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    // Create a new comment (optionally as a reply) on a post
    @Transactional
    public Comment createComment(User author, UUID postId, CreateCommentRequest request) {
        String body = request.getBody() == null ? "" : request.getBody().trim();
        if (!StringUtils.hasText(body)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment body is required");
        }

        Post post = postRepository
                .findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        Comment parentComment = null;
        if (request.getParentCommentId() != null) {
            parentComment = commentRepository
                    .findById(request.getParentCommentId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Parent comment not found"));
            if (!post.getId().equals(parentComment.getPost().getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Parent comment belongs to a different post");
            }
        }

        Comment comment = new Comment(post, author, body, parentComment);
        return commentRepository.save(comment);
    }

    // Get a single comment by ID or throw if not found
    public Comment getComment(UUID id) {
        return commentRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
    }

    // Get all comments (any state)
    public List<Comment> getAllComments() {
        return commentRepository.findAll();
    }

    // Get visible comments for a post ordered by creation time
    public List<Comment> getCommentsByPostId(UUID postId) {
        return commentRepository.findByPost_IdAndStateOrderByCreatedAtAsc(postId, CommentState.VISIBLE);
    }

    // Get recent visible comments by author
    public List<Comment> getCommentsByAuthor(UUID authorId) {
        return commentRepository.findByAuthor_IdAndStateOrderByCreatedAtDesc(authorId, CommentState.VISIBLE);
    }

    // Count visible comments for a post
    public long countVisibleCommentsByPost(UUID postId) {
        return commentRepository.countByPost_IdAndState(postId, CommentState.VISIBLE);
    }

    // Update the score of a comment by a delta
    @Transactional
    public void updateScore(UUID commentId, int delta) {
        commentRepository.updateScore(commentId, delta);
    }

    // Update a comment body, validating author and state
    @Transactional
    public Comment updateComment(UUID commentId, UUID userId, UpdateCommentRequest request) {
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can edit this comment");
        }
        if (comment.getState() != CommentState.VISIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot edit a non-visible comment");
        }
        String body = request.getBody() == null ? "" : request.getBody().trim();
        if (!StringUtils.hasText(body)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment body is required");
        }
        comment.setBody(body);
        return commentRepository.save(comment);
    }

    // Soft-delete a comment by marking it deleted by the author
    @Transactional
    public void deleteComment(UUID commentId, UUID userId) {
        Comment comment = getComment(commentId);
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can delete this comment");
        }
        comment.setState(CommentState.DELETED_BY_AUTHOR);
        commentRepository.save(comment);
    }
}
