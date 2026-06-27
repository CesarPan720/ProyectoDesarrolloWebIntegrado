package com.example.veterinaria.RestController;
import com.example.veterinaria.Model.Rol;
import com.example.veterinaria.Repository.MascotaRepository;
import com.example.veterinaria.Repository.UsuarioRepository;
import com.example.veterinaria.Repository.CitaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "http://localhost:4200")
public class AdminRestController {

    private final UsuarioRepository usuarioRepository;
    private final MascotaRepository mascotaRepository;
    private final CitaRepository citaRepository;

    public AdminRestController(UsuarioRepository usuarioRepository, MascotaRepository mascotaRepository, CitaRepository citaRepository) {
        this.usuarioRepository = usuarioRepository;
        this.mascotaRepository = mascotaRepository;
        this.citaRepository = citaRepository;
    }

    @GetMapping("/dashboard/stats")
    public ResponseEntity<Map<String, Object>> getDashboardStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsuarios", usuarioRepository.count());
        stats.put("totalVeterinarios", usuarioRepository.countByRol(Rol.VETERINARIO));
        stats.put("totalPacientes", mascotaRepository.count());
        stats.put("citasProgramadas", citaRepository.count());

        return ResponseEntity.ok(stats);
    }
}
