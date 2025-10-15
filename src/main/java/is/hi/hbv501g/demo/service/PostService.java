package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.CreatePostRequest;
import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.PostState;
import is.hi.hbv501g.demo.entity.PostType;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.repository.CommunityRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import is.hi.hbv501g.demo.repository.UserRepository;
import is.hi.hbv501g.demo.util.MediaStorage;
import is.hi.hbv501g.demo.util.SearchIndexer;
import java.io.IOException;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;
    private final MediaStorage mediaStorage;
    private final SearchIndexer searchIndexer;

    public PostService(
            PostRepository postRepository,
            CommunityRepository communityRepository,
            UserRepository userRepository,
            MediaStorage mediaStorage,
            SearchIndexer searchIndexer) {
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
        this.mediaStorage = mediaStorage;
        this.searchIndexer = searchIndexer;
    }

    @Transactional
    public Post createPost(UUID authorId, CreatePostRequest request, MultipartFile mediaFile) {
        Community community = communityRepository
                .findByNameIgnoreCase(request.getCommunityName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found"));
        User author = userRepository
                .findById(authorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        PostType type = request.getType();
        Post post = new Post(community, author, request.getTitle().trim(), type);
        post.setState(PostState.VISIBLE);

        switch (type) {
            case TEXT -> handleTextPost(request, post);
            case LINK -> handleLinkPost(request, post);
            case MEDIA -> handleMediaPost(request, post, mediaFile);
        }

        Post saved = postRepository.save(post);
        searchIndexer.indexPost(saved.getId());
        return saved;
    }

    public Post getPost(UUID id) {
        return postRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found"));
    }

    private void handleTextPost(CreatePostRequest request, Post post) {
        if (!StringUtils.hasText(request.getBody())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text posts require a body");
        }
        post.setBody(request.getBody().trim());
    }

    private void handleLinkPost(CreatePostRequest request, Post post) {
        if (!StringUtils.hasText(request.getUrl())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Link posts require a URL");
        }
        post.setUrl(request.getUrl().trim());
        if (StringUtils.hasText(request.getBody())) {
            post.setBody(request.getBody().trim());
        }
    }

    private void handleMediaPost(CreatePostRequest request, Post post, MultipartFile mediaFile) {
        if (mediaFile == null || mediaFile.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Media posts require an upload");
        }
        try {
            String mediaUrl = mediaStorage.upload(mediaFile);
            post.setMediaUrl(mediaUrl);
            if (StringUtils.hasText(request.getBody())) {
                post.setBody(request.getBody().trim());
            }
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to upload media");
        }
    }
}
