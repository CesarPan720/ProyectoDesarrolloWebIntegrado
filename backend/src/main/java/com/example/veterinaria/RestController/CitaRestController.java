package com.example.veterinaria.RestController;

import com.example.veterinaria.DTO.CitaDTO;
import com.example.veterinaria.Model.Cita;
import com.example.veterinaria.Service.CitaService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/citas")
@CrossOrigin(origins = "http://localhost:4200")
public class CitaRestController {

    private final CitaService citaService;

    public CitaRestController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    public ResponseEntity<List<Cita>> obtenerCitas(Authentication auth) {
        List<Cita> citas = citaService.obtenerCitasPorUsuario(auth.getName());
        return ResponseEntity.ok(citas);
    }

    @PostMapping
    public ResponseEntity<?> agendarCita(@RequestBody CitaDTO dto, Authentication auth) {
        try {
            Cita citaGuardada = citaService.agendarCita(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(citaGuardada);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id, @RequestBody String nuevoEstado) {
        try {
            Cita citaActualizada = citaService.actualizarEstado(id, nuevoEstado);
            return ResponseEntity.ok(citaActualizada);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}