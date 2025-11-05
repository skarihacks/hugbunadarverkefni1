package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.CreatePostRequest;
import is.hi.hbv501g.demo.dto.UpdatePostRequest;
import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.PostState;
import is.hi.hbv501g.demo.entity.PostType;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.repository.CommunityRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import java.util.Base64;
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
        byte[] mediaData = null;
        if (type == PostType.MEDIA) {
            if (!StringUtils.hasText(request.getMediaBase64())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media data is required for media posts");
            }
            try {
                mediaData = Base64.getDecoder().decode(request.getMediaBase64().trim());
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media data is not valid base64");
            }
        } else if (type != PostType.TEXT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported post type");
        }

        Community community = communityRepository
                .findByNameIgnoreCase(communityName)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found"));

        Post post = new Post(community, author, title, type);
        post.setBody(StringUtils.hasText(body) ? body : null);
        post.setMediaData(mediaData);
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

    @Transactional(readOnly = true)
    public List<Post> getPostsByAuthor(UUID authorId) {
        return postRepository.findByAuthor_IdAndStateOrderByCreatedAtDesc(authorId, PostState.VISIBLE);
    }

    @Transactional(readOnly = true)
    public List<Post> getVisiblePosts() {
        return postRepository.findAll().stream()
                .filter(post -> post.getState() == PostState.VISIBLE)
                .toList();
    }

    @Transactional
    public Post updatePost(UUID postId, UUID userId, UpdatePostRequest request) {
        Post post = getPost(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can edit this post");
        }
        if (post.getState() != PostState.VISIBLE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot edit a hidden post");
        }

        if (StringUtils.hasText(request.getTitle())) {
            post.setTitle(request.getTitle().trim());
        }
        if (request.getBody() != null) {
            String trimmed = request.getBody().trim();
            if (post.getType() == PostType.TEXT && !StringUtils.hasText(trimmed)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Body is required for text posts");
            }
            post.setBody(StringUtils.hasText(trimmed) ? trimmed : null);
        }
        if (request.getMediaBase64() != null) {
            if (post.getType() != PostType.MEDIA) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This post does not accept media content");
            }
            if (!StringUtils.hasText(request.getMediaBase64())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media data is required for media posts");
            }
            try {
                post.setMediaData(Base64.getDecoder().decode(request.getMediaBase64().trim()));
            } catch (IllegalArgumentException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media data is not valid base64");
            }
        }
        return postRepository.save(post);
    }

    @Transactional
    public void deletePost(UUID postId, UUID userId) {
        Post post = getPost(postId);
        if (!post.getAuthor().getId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the author can delete this post");
        }
        post.setState(PostState.HIDDEN);
        postRepository.save(post);
    }
}
