package com.example.veterinaria.RestController;

import com.example.veterinaria.Model.Cita;
import com.example.veterinaria.Model.EstadoCita;
import com.example.veterinaria.Model.HistorialMedico;
import com.example.veterinaria.Repository.CitaRepository;
import com.example.veterinaria.Repository.HistorialMedicoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/historiales")
@CrossOrigin(origins = "http://localhost:4200")
public class HistorialMedicoRestController {

    private final HistorialMedicoRepository historialRepository;
    private final CitaRepository citaRepository;

    public HistorialMedicoRestController(HistorialMedicoRepository historialRepository, CitaRepository citaRepository) {
        this.historialRepository = historialRepository;
        this.citaRepository = citaRepository;
    }

    @PostMapping("/cita/{citaId}")
    public ResponseEntity<?> registrarAtencion(@PathVariable Long citaId, @RequestBody HistorialMedico historial) {
        Optional<Cita> citaOptional = citaRepository.findById(citaId);

        if (citaOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cita no encontrada");
        }

        Cita cita = citaOptional.get();

        // 🛑 VALIDACIÓN: Verifica que la cita tenga un veterinario asignado
        if (cita.getVeterinario() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("No se puede registrar el historial: La cita no tiene un veterinario asignado.");
        }

        historial.setMascota(cita.getMascota());
        historial.setVeterinario(cita.getVeterinario());
        historial.setFechaAtencion(LocalDateTime.now());

        HistorialMedico guardado = historialRepository.save(historial);

        cita.setEstado(EstadoCita.COMPLETADA);
        citaRepository.save(cita);

        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<HistorialMedico>> obtenerHistorialMascota(@PathVariable Long mascotaId) {
        List<HistorialMedico> lista = historialRepository.findByMascotaIdOrderByFechaAtencionDesc(mascotaId);
        return ResponseEntity.ok(lista);
    }
}