package com.example.veterinaria.RestController;

import com.example.veterinaria.DTO.LoginDTO;
import com.example.veterinaria.DTO.RegistroDTO;
import com.example.veterinaria.Service.AuthService;
import com.example.veterinaria.Service.DniApiService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthRestController {

    private final AuthService authService;
    private final DniApiService dniApiService;

    public AuthRestController(AuthService authService, DniApiService dniApiService) {
        this.authService = authService;
        this.dniApiService = dniApiService;
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

    @GetMapping("/buscar-dni/{dni}")
    public ResponseEntity<?> buscarDniPeru(@PathVariable String dni) {
        try {
            Map<String, Object> respuesta = dniApiService.consultarDni(dni);
            return ResponseEntity.ok(respuesta);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Error al consultar el DNI"));
        }
    }
}