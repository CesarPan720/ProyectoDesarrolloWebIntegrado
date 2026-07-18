package com.example.veterinaria.RestController;
import com.example.veterinaria.Model.Cita;
import com.example.veterinaria.Model.Diagnostico;
import com.example.veterinaria.Model.EstadoCita;
import com.example.veterinaria.Repository.CitaRepository;
import com.example.veterinaria.Repository.DiagnosticoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/diagnosticos")
@CrossOrigin(origins = "http://localhost:4200")
public class DiagnosticoRestController {
    private final DiagnosticoRepository diagnosticoRepository;
    private final CitaRepository citaRepository;

    public DiagnosticoRestController(DiagnosticoRepository diagnosticoRepository, CitaRepository citaRepository) {
        this.diagnosticoRepository = diagnosticoRepository;
        this.citaRepository = citaRepository;
    }

    // POST: http://localhost:8080/api/diagnosticos/cita/{citaId}
    // POST: http://localhost:8080/api/diagnosticos/cita/{citaId}
    @PostMapping("/cita/{citaId}")
    public ResponseEntity<?> registrarDiagnostico(@PathVariable Long citaId, @RequestBody Diagnostico diagnostico) {
        if (!citaRepository.existsById(citaId)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Cita no encontrada");
        }

        Cita cita = citaRepository.findById(citaId).get();
        
        // 1. Asociamos la cita al diagnóstico
        diagnostico.setCita(cita);
        Diagnostico guardado = diagnosticoRepository.save(diagnostico);

        // 2. Cambiamos el estado de la cita a COMPLETADA
        cita.setEstado(EstadoCita.COMPLETADA);
        citaRepository.save(cita);

        return ResponseEntity.status(HttpStatus.CREATED).body(guardado);
    }

    // GET: http://localhost:8080/api/diagnosticos/cita/{citaId}
    @GetMapping("/cita/{citaId}")
    public ResponseEntity<?> obtenerDiagnosticoPorCita(@PathVariable Long citaId) {
        return diagnosticoRepository.findByCitaId(citaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}
