package is.hi.hbv501g.demo.repository;

import is.hi.hbv501g.demo.entity.Post;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Post p set p.score = p.score + :delta where p.id = :id")
    void updateScore(@Param("id") UUID id, @Param("delta") int delta);
}
