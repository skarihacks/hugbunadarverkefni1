package is.hi.hbv501g.demo.controller;

import is.hi.hbv501g.demo.dto.LoginRequest;
import is.hi.hbv501g.demo.dto.RegisterRequest;
import is.hi.hbv501g.demo.entity.User;
import is.hi.hbv501g.demo.service.AuthService;
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

    public static final String SESSION_HEADER = "X-Session-Id";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    //get request for registering a new user
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.from(user));
    }

    //post request for logging in a user
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        var session = authService.login(request);
        User user = authService.requireUser(session.token());
        LoginResponse body = new LoginResponse(session.token(), UserResponse.from(user));
        return ResponseEntity.ok()
                .header(SESSION_HEADER, session.token())
                .body(body);
    }
    //post request for logging out a user
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @RequestHeader(value = SESSION_HEADER, required = false) String sessionId) {
        authService.logout(sessionId);
        return ResponseEntity.noContent().build();
    }

    //response for a successful login
    public record LoginResponse(String sessionId, UserResponse user) {}


    //response for user information
    public record UserResponse(String id, String username, String email) {

        static UserResponse from(User user) {
            return new UserResponse(user.getId().toString(), user.getUsername(), user.getEmail());
        }
    }
}
