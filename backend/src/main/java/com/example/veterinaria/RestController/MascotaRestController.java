package com.example.veterinaria.RestController;

import com.example.veterinaria.DTO.MascotaDTO;
import com.example.veterinaria.Model.Mascota;
import com.example.veterinaria.Service.MascotaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "http://localhost:4200")
public class MascotaRestController {

    private final MascotaService mascotaService;

    public MascotaRestController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> obtenerMascotas(Authentication auth) {
        return ResponseEntity.ok(mascotaService.obtenerMascotas(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<?> registrarMascota(@Valid @RequestBody MascotaDTO dto, Authentication auth) {
        Mascota nueva = mascotaService.registrarMascota(dto, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMascota(@PathVariable Long id, @Valid @RequestBody MascotaDTO dto, Authentication auth) {
        Mascota actualizada = mascotaService.actualizarMascota(id, dto, auth.getName());
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMascota(@PathVariable Long id, Authentication auth) {
        mascotaService.eliminarMascota(id, auth.getName());
        return ResponseEntity.ok(Collections.singletonMap("mensaje", "Mascota eliminada correctamente"));
    }
}