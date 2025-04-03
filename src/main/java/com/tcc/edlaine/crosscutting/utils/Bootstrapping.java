package com.tcc.edlaine.crosscutting.utils;

import com.tcc.edlaine.domain.entities.UserEntity;
import com.tcc.edlaine.domain.enums.PermissionLevel;
import com.tcc.edlaine.repository.UserRepository;
import com.tcc.edlaine.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class Bootstrapping implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;
    private final AuthService authService;


    @Override
    public void run(String... args) throws Exception {
        seedUsers();
    }

    private void seedUsers() {
        List<UserEntity> users = List.of(
                new UserEntity("Edlaine Nunes", "11111111111", "edlaine.nunesr@gmail.com", authService.encriptPassword( "edlaine123"), PermissionLevel.SUPER_ADMIN, true),
                new UserEntity("Felipe Mota", "12345639586", "felipe@gmail.com", authService.encriptPassword("felipe"), PermissionLevel.SUPER_ADMIN, true),
                new UserEntity("Cleaine Oliveira", "86354968752", "cleiane@gmail.com", authService.encriptPassword("cleiane"), PermissionLevel.SUPER_ADMIN, true),
                new UserEntity("Avaliador", "68935742956", "avaliador@gmail.com", authService.encriptPassword("avaliador"), PermissionLevel.SUPER_ADMIN, true),

                new UserEntity("User Super Admin", "22222222222", "super.admin@gmail.com", authService.encriptPassword("edlaine123"), PermissionLevel.SUPER_ADMIN, true),
                new UserEntity("User Admin", "33333333333", "admin@gmail.com", authService.encriptPassword("edlaine123"), PermissionLevel.ADMIN, true),
                new UserEntity("User Counter", "44444444444", "counter@gmail.com", authService.encriptPassword("edlaine123"), PermissionLevel.COUNTER, true),
                new UserEntity("User User", "55555555555", "user@gmail.com", authService.encriptPassword("edlaine123"), PermissionLevel.USER, true),
                new UserEntity("User Guest", "66666666666", "guest@gmail.com", authService.encriptPassword("edlaine123"), PermissionLevel.GUEST, true),
                new UserEntity("User Admin Inactive", "77777777777", "admin.inactive@gmail.com", authService.encriptPassword("edlaine123"), PermissionLevel.ADMIN, false),
                new UserEntity("User Counter Inactive", "88888888888", "counter.inactive@gmail.com", authService.encriptPassword("edlaine123"), PermissionLevel.COUNTER, false)

        );

        List<UserEntity> usersToSave = new ArrayList<>();

        for (UserEntity user : users) {
            if (!userRepository.existsByEmail(user.getEmail())) {
                usersToSave.add(user);
            }
        }

        if (!usersToSave.isEmpty()) {
            userRepository.saveAll(usersToSave);
            System.out.println("✅ Novos usuários cadastrados no MongoDB!");
        } else {
            System.out.println("⚠️ Nenhum novo usuário foi cadastrado. Todos já existem no banco.");
        }

    }

}
