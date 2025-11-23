package is.hi.hbv501g.demo.service;

import is.hi.hbv501g.demo.dto.LoginRequest;
import is.hi.hbv501g.demo.dto.RegisterRequest;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.entity.UserStatus;
import is.hi.hbv501g.demo.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private static final HexFormat HEX = HexFormat.of();

    private final UserRepository userRepository;
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Register a new user with validation and password hashing
    @Transactional
    public User register(RegisterRequest request) {
        String username = normalize(request.getUsername());
        String email = normalize(request.getEmail());
        String password = request.getPassword();

        if (!StringUtils.hasText(username) || username.length() < 3) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username must be at least 3 characters");
        }
        if (!username.matches("^[A-Za-z0-9_]+$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username can only contain letters, numbers and underscores");
        }
        if (!StringUtils.hasText(email) || !email.contains("@")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email must be provided");
        }
        if (password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be provided");
        }

        if (userRepository.existsByUsernameIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already taken");
        }
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        String passwordHash = hashPassword(password);
        User user = new User(username, email.toLowerCase(), passwordHash);
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    // Authenticate a user and create a new session
    @Transactional(readOnly = true)
    public Session login(LoginRequest request) {
        String identifier = normalize(request.getUsernameOrEmail());
        String password = request.getPassword();

        if (!StringUtils.hasText(identifier) || !StringUtils.hasText(password)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username/email and password are required");
        }

        User user = userRepository
                .findByUsernameOrEmail(identifier)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Account is disabled");
        }

        if (!verifyPassword(password, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        Session session = new Session(UUID.randomUUID().toString(), user.getId(), Instant.now());
        sessions.put(session.token(), session);
        return session;
    }

    // Log out a user by removing their session
    public void logout(String sessionId) {
        if (sessionId == null || sessionId.isBlank()) {
            return;
        }
        sessions.remove(sessionId);
    }

    // Retrieve a valid session or throw if missing/invalid
    public Session requireSession(String sessionId) {
        Session session = sessions.get(sessionId);
        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Session invalid or expired");
        }
        return session;
    }

    // Retrieve the user associated with a session or throw if not found
    public User requireUser(String sessionId) {
        Session session = requireSession(sessionId);
        return userRepository
                .findById(session.userId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HEX.formatHex(hashed);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private boolean verifyPassword(String rawPassword, String storedHash) {
        return hashPassword(rawPassword).equals(storedHash);
    }

    private String normalize(String value) {
        return value == null ? null : value.trim();
    }

    public record Session(String token, UUID userId, Instant createdAt) {}
}
