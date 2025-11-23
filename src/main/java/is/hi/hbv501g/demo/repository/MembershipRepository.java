package is.hi.hbv501g.demo.repository;

import is.hi.hbv501g.demo.entity.Community;
import is.hi.hbv501g.demo.entity.Membership;
import is.hi.hbv501g.demo.entity.User;
import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MembershipRepository extends JpaRepository<Membership, UUID> {

    // Check if a membership exists for a user in a community
    boolean existsByUserAndCommunity(User user, Community community);

    // Find a membership by user and community
    Optional<Membership> findByUserAndCommunity(User user, Community community);

    // Find a membership by user ID and community ID
    Optional<Membership> findByUser_IdAndCommunity_Id(UUID userId, UUID communityId);

    // Find all memberships for a given user ID
    List<Membership> findByUser_Id(UUID userId);
}
