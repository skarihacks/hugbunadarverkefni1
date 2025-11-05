package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.entity.Comment;
import is.hi.hbv501g.demo.entity.Membership;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.entity.Vote;
import is.hi.hbv501g.demo.repository.CommentRepository;
import is.hi.hbv501g.demo.repository.CommunityRepository;
import is.hi.hbv501g.demo.repository.MembershipRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import is.hi.hbv501g.demo.repository.UserRepository;
import is.hi.hbv501g.demo.repository.VoteRepository;
import java.util.Base64;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DataDumpService {

    private final UserRepository userRepository;
    private final CommunityRepository communityRepository;
    private final MembershipRepository membershipRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final VoteRepository voteRepository;

    public DataDumpService(
            UserRepository userRepository,
            CommunityRepository communityRepository,
            MembershipRepository membershipRepository,
            PostRepository postRepository,
            CommentRepository commentRepository,
            VoteRepository voteRepository) {
        this.userRepository = userRepository;
        this.communityRepository = communityRepository;
        this.membershipRepository = membershipRepository;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.voteRepository = voteRepository;
    }

    public DumpResponse dumpAll() {
        List<UserDump> users = userRepository.findAll().stream()
                .map(user -> new UserDump(
                        user.getId().toString(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getStatus().name(),
                        user.getCreatedAt().toString()))
                .toList();

        List<CommunityDump> communities = communityRepository.findAll().stream()
                .map(community -> new CommunityDump(
                        community.getId().toString(),
                        community.getName(),
                        community.getDescription(),
                        community.getCreatedAt().toString()))
                .toList();

        List<MembershipDump> memberships = membershipRepository.findAll().stream()
                .map(membership -> new MembershipDump(
                        membership.getId().toString(),
                        membership.getUser().getId().toString(),
                        membership.getCommunity().getId().toString(),
                        membership.getRole().name(),
                        membership.getJoinedAt().toString()))
                .toList();

        List<PostDump> posts = postRepository.findAll().stream()
                .map(post -> new PostDump(
                        post.getId().toString(),
                        post.getCommunity().getId().toString(),
                        post.getAuthor().getId().toString(),
                        post.getTitle(),
                        post.getBody(),
                        encode(post),
                        post.getType().name(),
                        post.getScore(),
                        post.getState().name(),
                        post.getCreatedAt().toString()))
                .toList();

        List<CommentDump> comments = commentRepository.findAll().stream()
                .map(comment -> new CommentDump(
                        comment.getId().toString(),
                        comment.getPost().getId().toString(),
                        comment.getAuthor().getId().toString(),
                        comment.getParentComment() != null ? comment.getParentComment().getId().toString() : null,
                        comment.getBody(),
                        comment.getScore(),
                        comment.getState().name(),
                        comment.getCreatedAt().toString()))
                .toList();

        List<VoteDump> votes = voteRepository.findAll().stream()
                .map(vote -> new VoteDump(
                        vote.getId().toString(),
                        vote.getUser().getId().toString(),
                        vote.getTargetType().name(),
                        vote.getTargetId().toString(),
                        vote.getValue().name(),
                        vote.getCreatedAt().toString()))
                .toList();

        return new DumpResponse(users, communities, memberships, posts, comments, votes);
    }

    private String encode(Post post) {
        byte[] media = post.getMediaData();
        if (media == null || media.length == 0) {
            return null;
        }
        return Base64.getEncoder().encodeToString(media);
    }

    public record DumpResponse(
            List<UserDump> users,
            List<CommunityDump> communities,
            List<MembershipDump> memberships,
            List<PostDump> posts,
            List<CommentDump> comments,
            List<VoteDump> votes) {}

    public record UserDump(String id, String username, String email, String status, String createdAt) {}

    public record CommunityDump(String id, String name, String description, String createdAt) {}

    public record MembershipDump(String id, String userId, String communityId, String role, String joinedAt) {}

    public record PostDump(
            String id,
            String communityId,
            String authorId,
            String title,
            String body,
            String mediaBase64,
            String type,
            int score,
            String state,
            String createdAt) {}

    public record CommentDump(
            String id,
            String postId,
            String authorId,
            String parentCommentId,
            String body,
            int score,
            String state,
            String createdAt) {}

    public record VoteDump(
            String id, String userId, String targetType, String targetId, String value, String createdAt) {}
}
