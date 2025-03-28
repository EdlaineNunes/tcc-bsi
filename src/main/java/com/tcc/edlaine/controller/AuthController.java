package com.tcc.edlaine.controller;

import com.tcc.edlaine.crosscutting.utils.JwtTokenProvider;
import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.service.AuthService;
import io.jsonwebtoken.Claims;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthController(AuthService authService, JwtTokenProvider jwtTokenProvider) {
        this.authService = authService;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody UserEntity user) {
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        return ResponseEntity.ok(authService.authenticate(email, password));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getUserInfo(@RequestHeader("Authorization") String token) {
        token = token.replace("Bearer ", "");
        Claims claims = jwtTokenProvider.getClaims(token);

        Map<String, Object> response = new HashMap<>();
        response.put("email", claims.getSubject());
        response.put("name", claims.get("name"));
        response.put("role", claims.get("role"));

        return ResponseEntity.ok(response);
    }
}
