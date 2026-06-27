package com.example.veterinaria.Service;

import com.example.veterinaria.DTO.CitaDTO;
import com.example.veterinaria.Model.*;
import com.example.veterinaria.Repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CitaService {
    private final CitaRepository citaRepository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public CitaService(CitaRepository citaRepository, MascotaRepository mascotaRepository,
                       VeterinarioRepository veterinarioRepository, ClienteRepository clienteRepository,
                       UsuarioRepository usuarioRepository) {
        this.citaRepository = citaRepository;
        this.mascotaRepository = mascotaRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Cita> obtenerCitasPorUsuario(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.CLIENTE) {
            Cliente cliente = clienteRepository.findByUsuario(usuario).orElseThrow();
            List<Mascota> misMascotas = mascotaRepository.findByCliente(cliente);
            return citaRepository.findByMascotaIn(misMascotas);
        } else if (usuario.getRol() == Rol.VETERINARIO) {
            Veterinario vet = veterinarioRepository.findByUsuario(usuario).orElseThrow();
            return citaRepository.findByVeterinario(vet);
        }

        return citaRepository.findAll();
    }

    @Transactional
    public Cita agendarCita(CitaDTO dto) {
        Veterinario vet = veterinarioRepository.findById(dto.getVeterinarioId())
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        if (citaRepository.existsByVeterinarioAndFechaHora(vet, dto.getFechaHora())) {
            throw new IllegalArgumentException("El veterinario ya tiene una cita agendada en esa fecha y hora exacta.");
        }

        Mascota mascota = mascotaRepository.findById(dto.getMascotaId())
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        Cita cita = new Cita();
        cita.setMascota(mascota);
        cita.setVeterinario(vet);
        cita.setFechaHora(dto.getFechaHora());
        cita.setMotivo(dto.getMotivo());

        return citaRepository.save(cita);
    }

    @Transactional
    public Cita actualizarEstado(Long id, String nuevoEstado) {
        Cita cita = citaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cita no encontrada"));

        cita.setEstado(EstadoCita.valueOf(nuevoEstado.replace("\"", "").toUpperCase()));
        return citaRepository.save(cita);
    }
}