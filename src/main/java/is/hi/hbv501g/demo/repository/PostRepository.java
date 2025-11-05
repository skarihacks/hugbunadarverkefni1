package is.hi.hbv501g.demo.repository;

import is.hi.hbv501g.demo.entity.Post;
import is.hi.hbv501g.demo.entity.PostState;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {

    Page<Post> findByState(PostState state, Pageable pageable);

    Page<Post> findByCommunity_NameIgnoreCaseAndState(String communityName, PostState state, Pageable pageable);

    Page<Post> findByAuthor_IdAndState(UUID authorId, PostState state, Pageable pageable);

    List<Post> findByAuthor_IdAndStateOrderByCreatedAtDesc(UUID authorId, PostState state);

    @Query("select p from Post p where p.state = :state and lower(p.title) like lower(concat('%', :term, '%'))")
    Page<Post> searchByTitle(@Param("term") String term, @Param("state") PostState state, Pageable pageable);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Post p set p.score = p.score + :delta where p.id = :id")
    void updateScore(@Param("id") UUID id, @Param("delta") int delta);

    List<Post> findByTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(String title, String body);
}
