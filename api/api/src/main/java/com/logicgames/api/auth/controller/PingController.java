package com.logicgames.api.auth.controller;

import com.logicgames.api.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PingController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/api/ping-db")
    public String pingDb() {
        // Hacemos un "count" para obligar a encender la base de datos
        long usuarios = userRepository.count();
        return "Backend y Base de Datos despiertos. Total usuarios: " + usuarios;
    }
}
