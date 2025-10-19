package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.CreatePostRequest;
import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.PostState;
import is.hi.hbv501g.demo.entity.PostType;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.repository.CommunityRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;

    public PostService(PostRepository postRepository, CommunityRepository communityRepository) {
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
    }

    @Transactional
    public Post createPost(User author, CreatePostRequest request) {
        String communityName = request.getCommunityName() == null ? "" : request.getCommunityName().trim();
        String title = request.getTitle() == null ? "" : request.getTitle().trim();
        String body = request.getBody() == null ? "" : request.getBody().trim();
        PostType type = request.getType() == null ? PostType.TEXT : request.getType();

        if (!StringUtils.hasText(communityName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Community name is required");
        }
        if (!StringUtils.hasText(title)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Title is required");
        }
        if (type == PostType.TEXT && !StringUtils.hasText(body)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required for text posts");
        }
        if (type != PostType.TEXT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only text posts are supported in this sprint");
        }

        Community community = communityRepository
                .findByNameIgnoreCase(communityName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found"));

        Post post = new Post(community, author, title, type);
        post.setBody(body);
        post.setState(PostState.VISIBLE);
        return postRepository.save(post);
    }

    @Transactional(readOnly = true)
    public Post getPost(UUID id) {
        return postRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    @Transactional(readOnly = true)
    public List<Post> searchByTerm(String term) {
        String safeTerm = term == null ? "" : term.trim();
        return postRepository.findByTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(safeTerm, safeTerm);
    }
}
