package com.example.veterinaria.RestController;

import com.example.veterinaria.Service.DniApiService;
import com.example.veterinaria.Model.Usuario;
import com.example.veterinaria.Service.UsuarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/usuarios")
@CrossOrigin(origins = "http://localhost:4200")
public class UsuarioRestController {

    private final UsuarioService usuarioService;
    private final DniApiService dniApiService;

    public UsuarioRestController(UsuarioService usuarioService, DniApiService dniApiService) {
        this.usuarioService = usuarioService;
        this.dniApiService = dniApiService;
    }

    @GetMapping
    public ResponseEntity<List<Usuario>> listarTodos() {
        return ResponseEntity.ok(usuarioService.obtenerTodos());
    }

    @PostMapping
    public ResponseEntity<Usuario> crearUsuario(@RequestBody Map<String, String> payload) {
        Usuario usuarioGuardado = usuarioService.crearUsuario(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioGuardado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @RequestBody Usuario usuarioActualizado) {
        Usuario usuarioModificado = usuarioService.actualizarUsuario(id, usuarioActualizado);
        return ResponseEntity.ok(usuarioModificado);
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<Usuario> cambiarEstado(@PathVariable Long id) {
        Usuario usuarioModificado = usuarioService.cambiarEstado(id);
        return ResponseEntity.ok(usuarioModificado);
    }
}