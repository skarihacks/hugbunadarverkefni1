package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.entity.Comment;
import is.hi.hbv501g.demo.entity.CommentState;
import is.hi.hbv501g.demo.entity.MembershipRole;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.PostState;
import is.hi.hbv501g.demo.repository.CommentRepository;
import is.hi.hbv501g.demo.repository.MembershipRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class ModerationService {

    private final MembershipRepository membershipRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public ModerationService(
            MembershipRepository membershipRepository, PostRepository postRepository, CommentRepository commentRepository) {
        this.membershipRepository = membershipRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public void removePost(UUID userId, UUID postId) {
        Post post = postRepository
                .findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));

        if (!isModerator(userId, post.getCommunity().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Moderator access required");
        }

        post.setState(PostState.REMOVED_BY_MODERATOR);
        postRepository.save(post);
    }

    @Transactional
    public void removeComment(UUID userId, UUID commentId) {
        Comment comment = commentRepository
                .findById(commentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));

        if (!isModerator(userId, comment.getPost().getCommunity().getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Moderator access required");
        }

        comment.setState(CommentState.REMOVED_BY_MODERATOR);
        commentRepository.save(comment);
    }

    private boolean isModerator(UUID userId, UUID communityId) {
        return membershipRepository
                .findByUser_IdAndCommunity_Id(userId, communityId)
                .map(membership -> membership.getRole() == MembershipRole.MODERATOR
                        || membership.getRole() == MembershipRole.OWNER)
                .orElse(false);
    }
}
