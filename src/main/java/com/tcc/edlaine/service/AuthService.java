package com.tcc.edlaine.service;

import com.tcc.edlaine.crosscutting.exceptions.general.UserDuplicatedKeyException;
import com.tcc.edlaine.crosscutting.exceptions.user.UserAccessDenied;
import com.tcc.edlaine.crosscutting.exceptions.user.UserBadRequest;
import com.tcc.edlaine.crosscutting.exceptions.user.UserNotFound;
import com.tcc.edlaine.crosscutting.utils.JwtTokenProvider;
import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import com.tcc.edlaine.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Slf4j
@Service
public class AuthService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtTokenProvider jwtTokenProvider,
                       AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.authenticationManager = authenticationManager;
    }

    public String authenticate(String email, String password) {
        log.info("Initializing authentication for customer with email: {}", email);
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        return jwtTokenProvider.generateToken(user);
    }

    public String registerAuth(UserEntity user) {
        String psw = user.getPassword();
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setPermissionLevel(PermissionLevel.SUPER_ADMIN);
            userRepository.save(user);
            return authenticate(user.getEmail(), psw);
        }catch (DuplicateKeyException e) {
            log.error("Error registering user with duplicated ->: {}", user.getEmail());
            throw new UserDuplicatedKeyException("Error registering user with duplicated -> " + e.getMessage());
        } catch (Exception e) {
            log.error("Error registering user. {}", e.getMessage());
            throw new UserBadRequest("Error registering user -> " + e.getMessage());
        }
    }

    public HttpStatus register(UserEntity user) {
        try{
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setPermissionLevel(PermissionLevel.GUEST);
            user.setActive(true);
            userRepository.save(user);
            return HttpStatus.OK;
        }catch (DuplicateKeyException e) {
            log.error("Error registering user with duplicated ->: {}", user.getEmail());
            throw new UserDuplicatedKeyException("Error registering user with duplicated -> " + e.getMessage());
        } catch (Exception e) {
            log.error("Error registering user. {}", e.getMessage());
            throw new UserBadRequest("Error registering user -> " + e.getMessage());
        }
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));

        return User.builder()
                .username(user.getEmail())
                .password(user.getPassword())
                .roles(user.getPermissionLevel().name())
                .build();
    }

    public UserEntity getAuthenticatedUser() {
        log.info("Initializing user authentication");
        String userEmail = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
        return userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new UserNotFound("userEmail {" + userEmail + "} notFound"));
    }

    public static void validateGuestAccess(UserEntity user) {
        if (!user.isActive()
                || user.getPermissionLevel() == PermissionLevel.GUEST) {
            throw new UserAccessDenied("Unable to upload!");
        }
    }

    public static void validateUserAccess(UserEntity user, String customerEmailDocument) {
        if (!user.isActive() ||
                (user.getPermissionLevel() == PermissionLevel.GUEST &&
                        !Objects.equals(user.getEmail(), customerEmailDocument))) {
            throw new UserAccessDenied("Unable to upload!");
        }
    }

    public static void validateAdminAccessOrOwnerData(UserEntity user, String customerEmailDocument){
        if (!user.isActive() ||
                (user.getPermissionLevel().getLevel() < PermissionLevel.ADMIN.getLevel() &&
                        !Objects.equals(user.getEmail(), customerEmailDocument))) {
            throw new UserAccessDenied("Unable to upload!");
        }
    }

    public static void validateAdminAccess(UserEntity user) {
        if (!user.isActive()
                || user.getPermissionLevel().getLevel() < PermissionLevel.ADMIN.getLevel()) {
            throw new UserAccessDenied("Unable to upload!");
        }
    }

    public static void validadeUserAndAuthoritySuperAdmin(UserEntity user) {
        if (!user.isActive()
                || user.getPermissionLevel().getLevel() < PermissionLevel.SUPER_ADMIN.getLevel()) {
            throw new UserAccessDenied("Unable to upload!");
        }
    }

}
