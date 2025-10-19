package is.hi.hbv501g.demo.repository;

import is.hi.hbv501g.demo.entity.User;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsernameIgnoreCase(String username);

    Optional<User> findByEmailIgnoreCase(String email);

    @Query("""
            select u
            from User u
            where lower(u.username) = lower(:identifier) or lower(u.email) = lower(:identifier)
            """)
    Optional<User> findByUsernameOrEmail(@Param("identifier") String identifier);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByEmailIgnoreCase(String email);

    @Query("select u from User u where lower(u.username) like lower(concat('%', :term, '%')) order by u.username asc")
    List<User> searchByUsername(@Param("term") String term);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            update User u
            set u.failedLoginAttempts = :attempts,
                u.accountLockedUntil = :lockedUntil
            where u.id = :id
            """)
    void updateLoginFailureState(
            @Param("id") UUID id,
            @Param("attempts") int attempts,
            @Param("lockedUntil") LocalDateTime lockedUntil);

    @Transactional
    @Modifying(clearAutomatically = true)
    @Query("""
            update User u
            set u.failedLoginAttempts = 0,
                u.accountLockedUntil = null,
                u.lastLoginAt = :loginTime,
                u.updatedAt = :loginTime
            where u.id = :id
            """)
    void recordSuccessfulLogin(
            @Param("id") UUID id,
            @Param("loginTime") LocalDateTime loginTime);
}
