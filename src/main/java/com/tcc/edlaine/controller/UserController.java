package com.tcc.edlaine.controller;

import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // Registrar um novo usuário
    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody UserEntity userEntity) {
        try {
            UserEntity savedUser = userService.registerUser(userEntity);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    // Buscar usuário por ID
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUser(@PathVariable String id) {
        try {
            UserEntity user = userService.getUserById(id);
            return ResponseEntity.ok(user);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Listar todos os usuários (admin)
    @GetMapping("/")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        List<UserEntity> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Atualizar dados do usuário
    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable String id, @RequestBody UserEntity userEntity) {
        try {
            UserEntity updatedUser = userService.updateUser(id, userEntity);
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

}
