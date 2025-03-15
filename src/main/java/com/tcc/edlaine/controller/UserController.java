package com.tcc.edlaine.controller;

import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @ResponseStatus(HttpStatus.OK)
    @PostMapping("/register")
    public ResponseEntity<UserEntity> registerUser(@RequestBody UserEntity userEntity) {
        return userService.registerUser(userEntity);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/{id}")
    public ResponseEntity<UserEntity> getUser(@PathVariable String id) {
        return userService.getUserById(id);
    }

    @ResponseStatus(HttpStatus.OK)
    @GetMapping("/")
    public ResponseEntity<List<UserEntity>> getAllUsers() {
        return userService.getAllUsers();
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}")
    public ResponseEntity<UserEntity> updateUser(@PathVariable String id,
                                                 @RequestBody UserEntity userEntity) {
        return userService.updateUser(id, userEntity);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/disable")
    public ResponseEntity<String> disableUser(@PathVariable String id) {
        return userService.changeStatusUser(id, false);
    }

    @ResponseStatus(HttpStatus.OK)
    @PutMapping("/{id}/enable")
    public ResponseEntity<String> enableUser(@PathVariable String id) {
        return userService.changeStatusUser(id, true);
    }

}
