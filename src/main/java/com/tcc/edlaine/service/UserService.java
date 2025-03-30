package com.tcc.edlaine.service;

import com.tcc.edlaine.crosscutting.exceptions.general.UnprocessableEntityErrorException;
import com.tcc.edlaine.crosscutting.exceptions.general.UserDuplicatedKeyException;
import com.tcc.edlaine.crosscutting.exceptions.user.UserBadRequest;
import com.tcc.edlaine.crosscutting.exceptions.user.UserNotFound;
import com.tcc.edlaine.crosscutting.exceptions.user.UserUnauthorized;
import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import com.tcc.edlaine.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final UserRepository userRepository;

    public ResponseEntity<UserEntity> registerUser(UserEntity userEntity) {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateAdminAccess(user);
            userEntity.setPassword(authService.encriptPassword(userEntity.getPassword()));
            userRepository.save(userEntity);
            userEntity.setPassword(null);
            return ResponseEntity.ok(userEntity);
        } catch (DuplicateKeyException e) {
            log.error("Error registering user with duplicated ->: {}", userEntity.getEmail());
            throw new UserDuplicatedKeyException("Error registering user with duplicated -> " + e.getMessage());
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
            user.setPassword(null);
            log.info("User retornado::: {}", recoveredUser);
            return ResponseEntity.ok(recoveredUser);
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
            existingUser.setPermissionLevel(userEntity.getPermissionLevel());
            userRepository.save(existingUser);

            existingUser.setPassword(null);
            return ResponseEntity.ok(existingUser);
        } catch (Exception e) {
            log.error("Error update user. {}", e.getMessage());
            throw new UnprocessableEntityErrorException("Error recovered user -> " + e.getMessage());
        }
    }

    public HttpStatus updatePassword(String id, String password) {
        try{
            UserEntity user = authService.getAuthenticatedUser();
            UserEntity existingUser = getUserById(id).getBody();
            assert existingUser != null;

            if(!Objects.equals(user.getId(), existingUser.getId())
                    && user.getPermissionLevel().getLevel() < PermissionLevel.ADMIN.getLevel()){
                throw new UserUnauthorized();
            }

            existingUser.setPassword(authService.encriptPassword(password));
            userRepository.save(existingUser);

            return HttpStatus.OK;
        } catch (Exception e) {
            log.error("Error update user. {}", e.getMessage());
            throw new UnprocessableEntityErrorException("Error recovered user -> " + e.getMessage());
        }
    }

    @Transactional
    public ResponseEntity<String> changeStatusUser(String id, boolean active) {
        try{
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateAdminAccess(user);
            UserEntity recoveredUser = userRepository.findById(id)
                    .orElseThrow(() -> new UserNotFound("user not found"));

            recoveredUser.setActive(active);
            userRepository.save(recoveredUser);
            return ResponseEntity.ok("Successfully change status user");
        }catch (Exception e) {
            throw new UserBadRequest("Error to change status user. Please try again.");
        }
    }

    public ResponseEntity<List<UserEntity>> getAllUsers() {
        try {
            UserEntity user = authService.getAuthenticatedUser();
            AuthService.validateAdminAccess(user);
            List<UserEntity> users = userRepository.findAll();
            users.forEach(userEntity -> userEntity.setPassword(null));
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            log.error("Error recovered user. {}", e.getMessage());
            throw new UnprocessableEntityErrorException("Error recovered user -> " + e.getMessage());
        }
        
    }
}
