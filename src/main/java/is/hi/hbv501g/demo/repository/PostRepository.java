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

    // Find posts by state with pagination
    Page<Post> findByState(PostState state, Pageable pageable);

    // Find posts by community name and state with pagination
    Page<Post> findByCommunity_NameIgnoreCaseAndState(String communityName, PostState state, Pageable pageable);

    // Find posts by author ID and state with pagination
    Page<Post> findByAuthor_IdAndState(UUID authorId, PostState state, Pageable pageable);

    // Find recent posts by author ID and state, ordered by creation date descending
    List<Post> findByAuthor_IdAndStateOrderByCreatedAtDesc(UUID authorId, PostState state);

    // Search posts by title containing a term (case-insensitive) and state, with pagination
    @Query("select p from Post p where p.state = :state and lower(p.title) like lower(concat('%', :term, '%'))")
    Page<Post> searchByTitle(@Param("term") String term, @Param("state") PostState state, Pageable pageable);

    // Update the score of a post by a given delta
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Post p set p.score = p.score + :delta where p.id = :id")
    void updateScore(@Param("id") UUID id, @Param("delta") int delta);

    // Find posts where title or body contains a term (case-insensitive)
    List<Post> findByTitleContainingIgnoreCaseOrBodyContainingIgnoreCase(String title, String body);
}
