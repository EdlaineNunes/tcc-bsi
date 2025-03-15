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
            userEntity.setPassword(null);
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
            user.setPassword(null);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            log.error("Error recovered user. {}", e.getMessage());
            throw new UnprocessableEntityErrorException("Error recovered user -> " + e.getMessage());
        }
    }

    //TODO: quando o dado em userEntity vier vazio, não deve atualizar, deve permanecer com o dado já cadastrado. Ajustar
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
//            if (StringUtils.isNotBlank(userEntity.getPassword())) {
//                existingUser.setPassword(passwordEncoder.encode(userEntity.getPassword()));
//            } else{
//                existingUser.setPassword(existingUser.getPassword());
//                existingUser.setPassword(passwordEncoder.encode("edlaine123"));
//            }
            existingUser.setPermissionLevel(userEntity.getPermissionLevel());
            userRepository.save(existingUser);

            existingUser.setPassword(null);
            return ResponseEntity.ok(existingUser);
        } catch (Exception e) {
            log.error("Error update user. {}", e.getMessage());
            throw new UnprocessableEntityErrorException("Error recovered user -> " + e.getMessage());
        }
    }

    //TODO: ao desativar um usuário, deslogar ele se estiver com token habilitado
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
