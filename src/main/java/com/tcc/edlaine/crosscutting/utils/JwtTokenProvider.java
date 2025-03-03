package com.tcc.edlaine.crosscutting.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtTokenProvider {

//    private final String SECRET_KEY = "secrect-key"; // Utilize uma chave secreta segura em produção
//    private final long EXPIRATION_TIME = 864_000_000; // 10 dias em milissegundos

    private static final String SECRET_KEY = "abcdefghijklmnopqrstuvxyz123456789012"; // 32 caracteres
    private final Key key = Keys.hmacShaKeyFor(SECRET_KEY.getBytes());


    // Gera um token JWT
    public String generateToken(String email, Key key) {
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 10)) // 10 horas
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Valida o token JWT
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Extrai o username do token JWT
    public String getUsernameFromToken(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}