package com.tcc.edlaine.service;

import com.tcc.edlaine.crosscutting.exceptions.user.UserAccessDenied;
import com.tcc.edlaine.crosscutting.exceptions.user.UserNotFound;
import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.repository.UserRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Primary
@Service
public class MyUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MyUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        UserEntity userEntity = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFound("user [" + email + "] not found"));

        if (!userEntity.isActive()) {
            throw new UserAccessDenied("user is not active");
        }

        return User.builder()
                .username(userEntity.getUsername())
                .password(userEntity.getPassword())
                .roles(userEntity.getPermissionLevel().name())
                .build();
    }
}