package is.hi.hbv501g.demo.repository;

import is.hi.hbv501g.demo.entity.Community;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<Community, UUID> {

    Optional<Community> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    @Query("select c from Community c where lower(c.name) like lower(concat('%', :term, '%')) order by c.name asc")
    List<Community> searchByName(@Param("term") String term);
}
