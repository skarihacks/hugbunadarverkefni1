package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.CommunityRequests;
import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.repository.CommunityRepository;
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

    public CommunityService(CommunityRepository communityRepository) {
        this.communityRepository = communityRepository;
    }

    public Community getById(UUID id) {
        return communityRepository
                .findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found"));
    }

    public Community getByName(String name) {
        return communityRepository
                .findByNameIgnoreCase(name)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Community not found"));
    }

    public List<Community> search(String term) {
        if (!StringUtils.hasText(term)) {
            return communityRepository.findAll();
        }
        return communityRepository.searchByName(term.trim());
    }

    @Transactional
    public Community create(CommunityRequests.CreateCommunityRequest request) {
        if (communityRepository.existsByNameIgnoreCase(request.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Community name already taken");
        }
        Community community = new Community(request.getName().trim(), request.getDescription());
        return communityRepository.save(community);
    }
}
