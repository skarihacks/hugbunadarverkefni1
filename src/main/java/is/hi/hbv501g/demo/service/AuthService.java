package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.LoginRequest;
import is.hi.hbv501g.demo.dto.RegisterRequest;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.entity.UserStatus;
import is.hi.hbv501g.demo.repository.UserRepository;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final int MAX_FAILED_ATTEMPTS = 5;
    private static final Duration LOCK_DURATION = Duration.ofMinutes(15);
    private static final Duration SESSION_TTL = Duration.ofHours(8);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsernameIgnoreCase(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already in use");
        }
        if (userRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), request.getEmail(), hashedPassword);
        user.setStatus(UserStatus.ACTIVE);
        user.setEmailVerified(true);

        return userRepository.save(user);
    }

    @Transactional
    public Session login(LoginRequest request) {
        User user = loadUserForLogin(request.getIdentifier());
        ensureAccountIsActive(user);

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            registerFailedAttempt(user);
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        clearFailedAttempts(user);
        Session session = createSession(user);
        sessions.put(session.token(), session);
        return session;
    }

    public void logout(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        sessions.remove(sessionId);
    }

    public Session requireSession(String sessionId) {
        Session session = getSession(sessionId);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session invalid or expired");
        }
        return session;
    }

    public User requireUser(String sessionId) {
        Session session = requireSession(sessionId);
        return userRepository
                .findById(session.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public Session getSession(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return null;
        }
        Session session = sessions.get(sessionId);
        if (session == null) {
            return null;
        }
        if (session.expiresAt().isBefore(Instant.now())) {
            sessions.remove(sessionId);
            return null;
        }
        return session;
    }

    private User loadUserForLogin(String identifier) {
        return userRepository
                .findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
    }

    private void ensureAccountIsActive(User user) {
        if (user.getStatus() == UserStatus.SUSPENDED || user.getStatus() == UserStatus.DELETED) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is inactive");
        }
        if (user.isLockedNow()) {
            throw new ResponseStatusException(HttpStatus.LOCKED, "Account temporarily locked");
        }
    }

    private void registerFailedAttempt(User user) {
        int attempts = user.getFailedLoginAttempts() + 1;
        LocalDateTime lockUntil = null;
        if (attempts >= MAX_FAILED_ATTEMPTS) {
            lockUntil = LocalDateTime.now().plus(LOCK_DURATION);
        }
        userRepository.updateLoginFailureState(user.getId(), attempts, lockUntil);
    }

    private void clearFailedAttempts(User user) {
        userRepository.recordSuccessfulLogin(user.getId(), LocalDateTime.now());
    }

    private Session createSession(User user) {
        // Quick in-memory store for the assignment; swap for Redis when sessions must survive restarts.
        String token = UUID.randomUUID().toString();
        Instant expiry = Instant.now().plus(SESSION_TTL);
        return new Session(token, user.getId(), expiry);
    }

    public record Session(String token, UUID userId, Instant expiresAt) {}
}
