package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.repository.CommunityRepository;
import is.hi.hbv501g.demo.repository.PostRepository;
import is.hi.hbv501g.demo.repository.UserRepository;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    private final PostRepository postRepository;
    private final CommunityRepository communityRepository;
    private final UserRepository userRepository;

    public SearchService(
            PostRepository postRepository, CommunityRepository communityRepository, UserRepository userRepository) {
        this.postRepository = postRepository;
        this.communityRepository = communityRepository;
        this.userRepository = userRepository;
    }

    public List<Post> searchPosts(String term) {
        String safeTerm = term == null ? "" : term.trim();
        return postRepository.findByTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(safeTerm, safeTerm);
    }

    public List<Community> searchCommunities(String term) {
        if (term == null || term.trim().isEmpty()) {
            return communityRepository.findAll();
        }
        return communityRepository.searchByName(term.trim());
    }

    public List<User> searchUsers(String term) {
        if (term == null || term.trim().isEmpty()) {
            return userRepository.findAll();
        }
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().toLowerCase().contains(term.trim().toLowerCase()))
                .toList();
    }
}
