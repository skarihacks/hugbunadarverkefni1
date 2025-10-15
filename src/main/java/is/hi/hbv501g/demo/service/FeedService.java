package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.PostState;
import is.hi.hbv501g.demo.model.FeedScope;
import is.hi.hbv501g.demo.model.FeedSort;
import is.hi.hbv501g.demo.repository.PostRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FeedService {

    private final PostRepository postRepository;

    public FeedService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public Page<Post> list(
            FeedScope scope, FeedSort sort, int page, int size, String communityName, UUID userId) {
        Pageable pageable = buildPageable(sort, page, size);
        return switch (scope) {
            case GLOBAL -> postRepository.findByState(PostState.VISIBLE, pageable);
            case COMMUNITY -> {
                if (communityName == null || communityName.isBlank()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "community parameter is required");
                }
                yield postRepository.findByCommunity_NameIgnoreCaseAndState(communityName, PostState.VISIBLE, pageable);
            }
            case USER -> {
                if (userId == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "userId is required");
                }
                yield postRepository.findByAuthor_IdAndState(userId, PostState.VISIBLE, pageable);
            }
        };
    }

    public Page<Post> searchByTitle(String term, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return postRepository.searchByTitle(term, PostState.VISIBLE, pageable);
    }

    private Pageable buildPageable(FeedSort sort, int page, int size) {
        Sort springSort = switch (sort) {
            case NEW -> Sort.by(Sort.Direction.DESC, "createdAt");
            case TOP -> Sort.by(Sort.Direction.DESC, "score").and(Sort.by(Sort.Direction.DESC, "createdAt"));
            case HOT -> Sort.by(Sort.Direction.DESC, "score").and(Sort.by(Sort.Direction.DESC, "createdAt"));
        };
        return PageRequest.of(Math.max(page, 0), Math.max(size, 1), springSort);
    }
}
