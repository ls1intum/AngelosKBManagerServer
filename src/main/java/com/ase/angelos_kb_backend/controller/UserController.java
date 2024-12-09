package com.ase.angelos_kb_backend.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ase.angelos_kb_backend.dto.UserDTO;
import com.ase.angelos_kb_backend.exception.UnauthorizedException;
import com.ase.angelos_kb_backend.service.AuthenticationService;
import com.ase.angelos_kb_backend.service.UserService;
import com.ase.angelos_kb_backend.util.JwtUtil;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final AuthenticationService authenticationService;
    private final JwtUtil jwtUtil;

    public UserController(UserService userService, UserDetailsService userDetailsService, AuthenticationService authenticationService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.authenticationService = authenticationService;
        this.jwtUtil = jwtUtil;
    }
    
    @GetMapping
    public ResponseEntity<List<UserDTO>> getAllUsers(@RequestHeader("Authorization") String token) {
        Long orgId = jwtUtil.extractOrgId(token.replace("Bearer ", ""));
        List<UserDTO> users = userService.getAllUsersByOrgId(orgId);
        return ResponseEntity.ok(users);
    }

    /**
     * Approve a user by ID.
     */
    @PatchMapping("/{userId}/approve")
    public ResponseEntity<UserDTO> approveUser(
            @RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        Long orgId = jwtUtil.extractOrgId(token.replace("Bearer ", ""));
        UserDTO updatedUser = userService.approveUser(userId, orgId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Set a user to admin by ID.
     */
    @PatchMapping("/{userId}/set-admin")
    public ResponseEntity<UserDTO> setUserToAdmin(
            @RequestHeader("Authorization") String token,
            @PathVariable Long userId) {
        Long orgId = jwtUtil.extractOrgId(token.replace("Bearer ", ""));
        UserDTO updatedUser = userService.setUserToAdmin(orgId, userId);
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDTO> registerUser(
            @RequestParam String mail,
            @RequestParam String password,
            @RequestParam Long orgId) {
        UserDTO newUser = userService.registerUser(mail, password, orgId);
        return ResponseEntity.ok(newUser);
    }

    /**
     * Confirm user email using the confirmation token.
     */
    @GetMapping("/confirm")
    public ResponseEntity<String> confirmEmail(@RequestParam("token") String token) {
        boolean isConfirmed = userService.confirmUserEmail(token);
        if (isConfirmed) {
            return ResponseEntity.ok("Email confirmed successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired confirmation token.");
        }
    }

    /**
     * Login endpoint.
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(
            @RequestParam String email,
            @RequestParam String password) {
        Map<String, String> tokens = authenticationService.login(email, password);
        return ResponseEntity.ok(tokens);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccessToken(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        String accessToken = request.get("accessToken");

        if (refreshToken == null || refreshToken.isEmpty() || accessToken == null || accessToken.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Refresh token is required"));
        }
        try {
            // Validate the refresh token
            String email = jwtUtil.extractEmail(refreshToken);
            String emailAccessToken = jwtUtil.extractEmail(accessToken);

            if (! email.equals(emailAccessToken)) {
                throw new UnauthorizedException("Invalid refresh token");
            }

            // Load user details (optional for additional validation)
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!jwtUtil.validateToken(refreshToken, userDetails)) {
                throw new UnauthorizedException("Invalid refresh token");
            }

            // Generate a new access token
            Long orgId = jwtUtil.extractOrgId(refreshToken);
            boolean isSystemAdmin = jwtUtil.extractIsSystemAdmin(refreshToken);
            String newAccessToken = jwtUtil.generateToken(email, orgId, isSystemAdmin);

            return ResponseEntity.ok(Map.of("accessToken", newAccessToken));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid or expired refresh token"));
        }
    }
}
