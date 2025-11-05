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

    boolean existsByUserAndCommunity(User user, Community community);

    Optional<Membership> findByUserAndCommunity(User user, Community community);

    Optional<Membership> findByUser_IdAndCommunity_Id(UUID userId, UUID communityId);

    List<Membership> findByUser_Id(UUID userId);
}
