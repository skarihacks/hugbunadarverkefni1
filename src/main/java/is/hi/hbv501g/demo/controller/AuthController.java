package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.LoginRequest;
import is.hi.hbv501g.demo.dto.RegisterRequest;
import is.hi.hbv501g.demo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SESSION_HEADER = "X-Session-Id";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserView> register(@Valid @RequestBody RegisterRequest request) {
        var user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserView.from(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthSessionView> login(@Valid @RequestBody LoginRequest request) {
        var session = authService.login(request);
        var user = authService.requireUser(session.token());
        AuthSessionView body = new AuthSessionView(session.token(), UserView.from(user));
        return ResponseEntity.ok()
                .header(SESSION_HEADER, session.token())
                .body(body);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader(value = SESSION_HEADER, required = false) String sessionId) {
        authService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }

    public record AuthSessionView(String sessionId, UserView user) {}

    public record UserView(String id, String username, String email, String status) {

        static UserView from(is.hi.hbv501g.demo.entity.User user) {
            return new UserView(
                    user.getId().toString(), user.getUsername(), user.getEmail(), user.getStatus().name());
        }
    }
}
