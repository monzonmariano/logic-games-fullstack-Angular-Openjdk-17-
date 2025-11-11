package com.logicgames.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/Hello")
    public Map<String, String> sayHello(){
        
        return Map.of("message","¡Hola desde Spring Boot!");
    }

    // Como esta ruta NO está en la lista "permitAll()"
// de SecurityConfig, ¡requerirá autenticación!
    @GetMapping("/secure-data")
    public Map<String, String> getSecureData() {
        return Map.of("message", "¡Este es un mensaje SECRETO solo para usuarios logueados!");
    }

}
