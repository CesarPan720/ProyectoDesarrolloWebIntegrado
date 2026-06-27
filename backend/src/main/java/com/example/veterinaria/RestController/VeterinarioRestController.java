package com.example.veterinaria.RestController;

import com.example.veterinaria.DTO.VeterinarioDTO;
import com.example.veterinaria.Model.Veterinario;
import com.example.veterinaria.Service.VeterinarioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/veterinarios")
@CrossOrigin(origins = "http://localhost:4200")
public class VeterinarioRestController {

    private final VeterinarioService veterinarioService;

    public VeterinarioRestController(VeterinarioService veterinarioService) {
        this.veterinarioService = veterinarioService;
    }

    @GetMapping
    public ResponseEntity<List<Veterinario>> listarTodos() {
        return ResponseEntity.ok(veterinarioService.obtenerTodos());
    }

    @PostMapping
    public ResponseEntity<Veterinario> crearVeterinario(@RequestBody VeterinarioDTO dto) {
        Veterinario nuevoVeterinario = veterinarioService.crearVeterinario(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoVeterinario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Veterinario> actualizarVeterinario(@PathVariable Long id, @RequestBody VeterinarioDTO dto) {
        Veterinario veterinarioActualizado = veterinarioService.actualizarVeterinario(id, dto);
        return ResponseEntity.ok(veterinarioActualizado);
    }
}