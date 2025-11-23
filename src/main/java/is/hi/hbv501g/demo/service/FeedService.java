package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.PostState;
import is.hi.hbv501g.demo.repository.MembershipRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class FeedService {

    private final PostRepository postRepository;
    private final MembershipRepository membershipRepository;

    public FeedService(PostRepository postRepository, MembershipRepository membershipRepository) {
        this.postRepository = postRepository;
        this.membershipRepository = membershipRepository;
    }

    // List public feed posts with optional sorting and community filter
    public List<Post> listFeed(String sort, String community) {
        String normalizedSort = sort == null ? "new" : sort.toLowerCase(Locale.ROOT);
        String filterCommunity = community == null ? null : community.trim().toLowerCase(Locale.ROOT);

        Comparator<Post> comparator = buildComparator(normalizedSort);

        return postRepository.findAll().stream()
                .filter(post -> post.getState() == PostState.VISIBLE)
                .filter(post -> filterCommunity == null
                        || post.getCommunity().getName().equalsIgnoreCase(filterCommunity))
                .sorted(comparator)
                .toList();
    }

    // List feed posts from communities the user is a member of
    public List<Post> listMemberFeed(String sort, UUID userId) {
        String normalizedSort = sort == null ? "new" : sort.toLowerCase(Locale.ROOT);
        Comparator<Post> comparator = buildComparator(normalizedSort);

        Set<UUID> communityIds = membershipRepository.findByUser_Id(userId).stream()
                .map(membership -> membership.getCommunity().getId())
                .collect(Collectors.toSet());

        if (communityIds.isEmpty()) {
            return List.of();
        }

        return postRepository.findAll().stream()
                .filter(post -> post.getState() == PostState.VISIBLE)
                .filter(post -> communityIds.contains(post.getCommunity().getId()))
                .sorted(comparator)
                .toList();
    }

    private Comparator<Post> buildComparator(String sortKey) {
        return switch (sortKey) {
            case "top" -> Comparator.comparingInt(Post::getScore).reversed()
                    .thenComparing(Post::getCreatedAt, Comparator.reverseOrder());
            default -> Comparator.comparing(Post::getCreatedAt, Comparator.reverseOrder());
        };
    }
}
