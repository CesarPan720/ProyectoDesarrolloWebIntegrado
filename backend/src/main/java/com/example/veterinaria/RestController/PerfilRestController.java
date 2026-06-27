package com.example.veterinaria.RestController;
import com.example.veterinaria.Model.*;
import com.example.veterinaria.Repository.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/perfil")
@CrossOrigin(origins = "http://localhost:4200")
public class PerfilRestController {
    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final VeterinarioRepository veterinarioRepository;

    public PerfilRestController(UsuarioRepository usuarioRepository,
                                ClienteRepository clienteRepository,
                                VeterinarioRepository veterinarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    @GetMapping("/nombre")
    public ResponseEntity<Map<String, String>> obtenerNombreUsuario(Authentication auth) {
        Usuario usuario = usuarioRepository.findByEmail(auth.getName()).orElseThrow();
        Map<String, String> respuesta = new HashMap<>();

        if (usuario.getRol() == Rol.CLIENTE) {
            Cliente cliente = clienteRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
            respuesta.put("nombre", cliente.getNombre());
        } else if (usuario.getRol() == Rol.VETERINARIO) {
            Veterinario vet = veterinarioRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("Perfil de veterinario no encontrado"));
            respuesta.put("nombre", vet.getNombre());
        } else {
            respuesta.put("nombre", "Administrador");
        }
        return ResponseEntity.ok(respuesta);
    }
}