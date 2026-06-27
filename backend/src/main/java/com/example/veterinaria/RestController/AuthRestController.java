package com.example.veterinaria.RestController;

import com.example.veterinaria.DTO.LoginDTO;
import com.example.veterinaria.DTO.RegistroDTO;
import com.example.veterinaria.Service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthRestController {

    private final AuthService authService;

    public AuthRestController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registrarUsuario(@Valid @RequestBody RegistroDTO registroDTO) {
        try {
            authService.registrar(registroDTO);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("mensaje", "Usuario registrado exitosamente"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUsuario(@RequestBody LoginDTO loginDTO) {
        try {
            String token = authService.login(loginDTO.getEmail(), loginDTO.getPassword());
            return ResponseEntity.ok(Collections.singletonMap("token", token));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}