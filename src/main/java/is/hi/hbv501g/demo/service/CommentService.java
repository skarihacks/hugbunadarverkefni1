package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.CreateCommentRequest;
import is.hi.hbv501g.demo.entity.Comment;
import is.hi.hbv501g.demo.entity.CommentState;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.PostState;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.repository.CommentRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import is.hi.hbv501g.demo.repository.UserRepository;
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
    private final UserRepository userRepository;

    public CommentService(
            CommentRepository commentRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Comment createComment(UUID userId, CreateCommentRequest request) {
        if (!StringUtils.hasText(request.getBody())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Comment body cannot be empty");
        }
        Post post = postRepository
                .findById(request.getPostId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
        if (post.getState() != PostState.VISIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot comment on hidden posts");
        }
        User author = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Comment comment = new Comment(post, author, request.getBody().trim());
        comment.setState(CommentState.VISIBLE);
        return commentRepository.save(comment);
    }

    public List<Comment> listVisible(UUID postId) {
        return commentRepository.findByPost_IdAndStateOrderByCreatedAtAsc(postId, CommentState.VISIBLE);
    }
}
