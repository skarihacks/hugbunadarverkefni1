package is.hi.hbv501g.demo.repository;

import is.hi.hbv501g.demo.entity.Vote;
import is.hi.hbv501g.demo.entity.VoteTargetType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VoteRepository extends JpaRepository<Vote, UUID> {

    Optional<Vote> findByUser_IdAndTargetIdAndTargetType(UUID userId, UUID targetId, VoteTargetType targetType);

    List<Vote> findAllByTargetIdAndTargetType(UUID targetId, VoteTargetType targetType);
}
