package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.VoteRequest;
import is.hi.hbv501g.demo.entity.CommentState;
import is.hi.hbv501g.demo.entity.PostState;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.entity.Vote;
import is.hi.hbv501g.demo.entity.VoteTargetType;
import is.hi.hbv501g.demo.entity.VoteValue;
import is.hi.hbv501g.demo.repository.CommentRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import is.hi.hbv501g.demo.repository.UserRepository;
import is.hi.hbv501g.demo.repository.VoteRepository;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class VoteService {

    private final VoteRepository voteRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;

    public VoteService(
            VoteRepository voteRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            UserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public Vote recordVote(UUID userId, VoteRequest request) {
        VoteTargetType targetType = request.getTargetType();
        UUID targetId = request.getTargetId();
        VoteValue newValue = mapDirection(request.getDirection());

        Vote existingVote = voteRepository
                .findByUser_IdAndTargetIdAndTargetType(userId, targetId, targetType)
                .orElse(null);

        VoteValue previousValue = existingVote != null ? existingVote.getValue() : VoteValue.NEUTRAL;
        int delta = valueToScore(newValue) - valueToScore(previousValue);

        if (delta != 0) {
            updateTargetScore(targetType, targetId, delta);
        }

        if (existingVote != null) {
            existingVote.setValue(newValue);
            return voteRepository.save(existingVote);
        }

        User user = userRepository
                .findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
        Vote vote = new Vote(user, targetType, targetId, newValue);
        return voteRepository.save(vote);
    }

    private VoteValue mapDirection(int direction) {
        return switch (direction) {
            case 1 -> VoteValue.UPVOTE;
            case -1 -> VoteValue.DOWNVOTE;
            default -> VoteValue.NEUTRAL;
        };
    }

    private int valueToScore(VoteValue value) {
        return switch (value) {
            case UPVOTE -> 1;
            case DOWNVOTE -> -1;
            case NEUTRAL -> 0;
        };
    }

    private void updateTargetScore(VoteTargetType targetType, UUID targetId, int delta) {
        switch (targetType) {
            case POST -> {
                var post = postRepository
                        .findById(targetId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
                if (post.getState() != PostState.VISIBLE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voting disabled for this post");
                }
                postRepository.updateScore(targetId, delta);
            }
            case COMMENT -> {
                var comment = commentRepository
                        .findById(targetId)
                        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found"));
                if (comment.getState() != CommentState.VISIBLE) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Voting disabled for this comment");
                }
                commentRepository.updateScore(targetId, delta);
            }
        }
    }
}
