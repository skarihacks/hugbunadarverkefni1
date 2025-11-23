package is.hi.hbv501g.demo.service;

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
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public UserService(UserRepository userRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.userRepository = userRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    // Get a user by username or throw if not found
    public User getByUsername(String username) {
        return userRepository
                .findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    // Get recent visible posts for a user
    public List<Post> getRecentPosts(UUID userId) {
        return postRepository.findByAuthor_IdAndStateOrderByCreatedAtDesc(userId, PostState.VISIBLE);
    }

    // Get recent visible comments for a user
    public List<Comment> getRecentComments(UUID userId) {
        return commentRepository.findByAuthor_IdAndStateOrderByCreatedAtDesc(userId, CommentState.VISIBLE);
    }
}
