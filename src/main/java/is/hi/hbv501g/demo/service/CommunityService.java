package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.CommunityRequests;
import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.entity.Membership;
import is.hi.hbv501g.demo.entity.MembershipRole;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.repository.CommunityRepository;
import is.hi.hbv501g.demo.repository.MembershipRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CommunityService {

    private final CommunityRepository communityRepository;
    private final MembershipRepository membershipRepository;

    public CommunityService(CommunityRepository communityRepository, MembershipRepository membershipRepository) {
        this.communityRepository = communityRepository;
        this.membershipRepository = membershipRepository;
    }

    // Create a new community and add the creator as owner
    @Transactional
    public Community createCommunity(User creator, CommunityRequests.CreateCommunityRequest request) {
        String name = request.getName() == null ? "" : request.getName().trim();
        String description = request.getDescription() == null ? null : request.getDescription().trim();

        if (!StringUtils.hasText(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Community name is required");
        }
        if (name.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Community name must be at least 3 characters");
        }

        if (communityRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Community name already in use");
        }

        Community community = new Community(name, description);
        Community saved = communityRepository.save(community);

        Membership membership = new Membership(creator, saved, MembershipRole.OWNER);
        membershipRepository.save(membership);

        return saved;
    }

    // Join a community, creating membership if it does not exist
    @Transactional
    public Membership joinCommunity(User user, String communityName) {
        if (!StringUtils.hasText(communityName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Community name is required");
        }

        Community community = communityRepository
                .findByNameIgnoreCase(communityName.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found"));

        if (membershipRepository.existsByUserAndCommunity(user, community)) {
            return membershipRepository.findByUserAndCommunity(user, community).orElseThrow();
        }

        Membership membership = new Membership(user, community, MembershipRole.MEMBER);
        return membershipRepository.save(membership);
    }

    // Leave a community, preventing owners from leaving their own community
    @Transactional
    public void leaveCommunity(User user, String communityName) {
        if (!StringUtils.hasText(communityName)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Community name is required");
        }

        Community community = communityRepository
                .findByNameIgnoreCase(communityName.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found"));

        Membership membership = membershipRepository
                .findByUserAndCommunity(user, community)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "You are not a member of this community"));

        if (membership.getRole() == MembershipRole.OWNER) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Owners cannot leave their own community");
        }

        membershipRepository.delete(membership);
    }

    // List communities, optionally filtered by search query
    @Transactional(readOnly = true)
    public List<Community> listCommunities(String query) {
        if (!StringUtils.hasText(query)) {
            return communityRepository.findAll();
        }
        return communityRepository.searchByName(query.trim());
    }

    // Get a single community by ID or throw if not found
    @Transactional(readOnly = true)
    public Community getById(UUID id) {
        return communityRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found"));
    }

    @Transactional(readOnly = true)
    public Community getByName(String name) {
        if (!StringUtils.hasText(name)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Community name is required");
        }
        return communityRepository
                .findByNameIgnoreCase(name.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found"));
    }
}
