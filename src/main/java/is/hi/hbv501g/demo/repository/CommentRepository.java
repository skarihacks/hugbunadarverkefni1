package is.hi.hbv501g.demo.repository;

import is.hi.hbv501g.demo.entity.Comment;
import is.hi.hbv501g.demo.entity.CommentState;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, UUID> {

    // Find comments by post ID and state, ordered by creation date ascending

    List<Comment> findByPost_IdAndStateOrderByCreatedAtAsc(UUID postId, CommentState state);

    // Find comments by author ID and state, ordered by creation date descending

    List<Comment> findByAuthor_IdAndStateOrderByCreatedAtDesc(UUID authorId, CommentState state);

    // Count comments by post ID and state

    long countByPost_IdAndState(UUID postId, CommentState state);

    // Update the score of a comment by a given delta

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("update Comment c set c.score = c.score + :delta where c.id = :id")
    void updateScore(@Param("id") UUID id, @Param("delta") int delta);
}
