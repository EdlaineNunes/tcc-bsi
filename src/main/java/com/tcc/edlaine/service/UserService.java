package com.tcc.edlaine.service;

import com.tcc.edlaine.crosscutting.exceptions.general.UnprocessableEntityErrorException;
import com.tcc.edlaine.crosscutting.exceptions.user.UserBadRequest;
import com.tcc.edlaine.crosscutting.exceptions.user.UserNotFound;
import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ResponseEntity<UserEntity> registerUser(UserEntity userEntity) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateAdminAccess(user);
            userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
            userRepository.save(userEntity);
            return ResponseEntity.ok(userEntity);
        } catch (Exception e) {
            log.error("Error registering user. {}", e.getMessage());
            throw new UserBadRequest("Error registering user -> " + e.getMessage());
        }
    }

    public ResponseEntity<UserEntity> getUserById(String id) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            UserEntity recoveredUser = userRepository.findById(id).orElseThrow(() -> new UserNotFound("user not found"));
            AuthService.validateAdminAccessOrOwnerData(user, recoveredUser.getEmail());
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error recovered user. {}", e.getMessage());
            throw new UnprocessableEntityErrorException("Error recovered user -> " + e.getMessage());
        }
    }

    public ResponseEntity<UserEntity> updateUser(String id, UserEntity userEntity) {
        try{
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateAdminAccess(user);
            UserEntity existingUser = getUserById(id).getBody();
            if(existingUser == null){
                throw new UserNotFound("user not found");
            }

            existingUser.setUsername(userEntity.getUsername());
            existingUser.setCpf(userEntity.getCpf());
            existingUser.setEmail(userEntity.getEmail());
            if (userEntity.getPassword() != null) {
                existingUser.setPassword(passwordEncoder.encode(userEntity.getPassword()));
            }
            existingUser.setPermissionLevel(userEntity.getPermissionLevel());
            userRepository.save(existingUser);

            existingUser.setPassword(null);
            return ResponseEntity.ok(existingUser);
        } catch (Exception e) {
            log.error("Error update user. {}", e.getMessage());
            throw new UnprocessableEntityErrorException("Error recovered user -> " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<String> deactivateUser(String id) {
        try{
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateAdminAccess(user);
            UserEntity recoveredUser = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFound("user not found"));

            recoveredUser.setActive(false);  // Marca o usuário como inativo
            userRepository.save(recoveredUser);  // Salva as alterações
            return ResponseEntity.ok("Successfully deactivated user");
        }catch (Exception e) {
            throw new UserBadRequest("Error to deactivated user. Please try again.");
        }
    }

    public ResponseEntity<List<UserEntity>> getAllUsers() {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateAdminAccess(user);
            List<UserEntity> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error recovered user. {}", e.getMessage());
            throw new UnprocessableEntityErrorException("Error recovered user -> " + e.getMessage());
        }
        
    }
}
