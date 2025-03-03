package com.tcc.edlaine.service;

import com.tcc.edlaine.crosscutting.utils.JwtTokenProvider;
import com.tcc.edlaine.domain.dto.UserEntity;
import com.tcc.edlaine.repository.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Slf4j
@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    private final JwtTokenProvider jwtTokenProvider;

    // A chave precisa ter 32 bytes para HMAC-SHA
    private static final String SECRET_KEY = "abcdefghijklmnopqrstuvxyz123456789012"; // 32 caracteres
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    public String authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        log.info("Usuário encontrado! User {} / pass {}", user.getEmail(), user.getSenha());
//        return Jwts.builder()
//                .setSubject(user.getEmail())
//                .setIssuedAt(new Date())
//                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 horas
//                .signWith(key, SignatureAlgorithm.HS256)
//                .compact();
        return jwtTokenProvider.generateToken(user.getEmail(), key);
    }

    public String register(UserEntity user) {
        // Codificando a senha usando PBKDF2PasswordEncoder
        String psw = user.getSenha();
        user.setSenha(passwordEncoder.encode(user.getSenha()));
        userRepository.save(user);
//        return "Success";
        return authenticate(user.getEmail(), psw);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));

        return User.builder()
                .username(user.getEmail())
                .password(user.getSenha())  // Senha já criptografada
                .roles(user.getPermissionLevel().name())
                .build();
    }
}
