package com.tcc.edlaine.service;

import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;  // Para codificar a senha

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Cadastrar novo usuário
    public UserEntity registerUser(UserEntity userEntity) {
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));  // Codificando a senha
        return userRepository.save(userEntity);
    }

    // Buscar usuário por ID
    public UserEntity getUserById(String id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
    }

    // Editar dados do usuário
    public UserEntity updateUser(String id, UserEntity userEntity) {
        UserEntity existingUser = getUserById(id);
        existingUser.setUsername(userEntity.getUsername());
        existingUser.setCpf(userEntity.getCpf());
        existingUser.setEmail(userEntity.getEmail());
        if (userEntity.getPassword() != null) {
            existingUser.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        }
        existingUser.setPermissionLevel(userEntity.getPermissionLevel());
        return userRepository.save(existingUser);
    }

    // Método para inativar o usuário
    @Transactional
    public void deactivateUser(String id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        user.setActive(false);  // Marca o usuário como inativo
        userRepository.save(user);  // Salva as alterações
    }


    // Listar todos os usuários (Admin)
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }
}
