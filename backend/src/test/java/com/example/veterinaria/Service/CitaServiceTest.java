package com.example.veterinaria.Service;

import com.example.veterinaria.DTO.CitaDTO;
import com.example.veterinaria.Model.Cita;
import com.example.veterinaria.Model.Mascota;
import com.example.veterinaria.Model.Veterinario;
import com.example.veterinaria.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class CitaServiceTest {

    // 1. Inyectamos todos los Mocks que usa actualmente el CitaService
    @Mock private CitaRepository citaRepository;
    @Mock private MascotaRepository mascotaRepository;
    @Mock private VeterinarioRepository veterinarioRepository;
    @Mock private ClienteRepository clienteRepository;
    @Mock private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CitaService citaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLanzarErrorSiVeterinarioNoExiste() {
        CitaDTO dto = new CitaDTO();
        dto.setVeterinarioId(99L); // ID que no existe

        when(veterinarioRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            citaService.agendarCita(dto);
        });

        assertEquals("Veterinario no encontrado", exception.getMessage());
    }

    @Test
    void testLanzarErrorSiVeterinarioEstaOcupado() {
        // Probamos el punto de la rúbrica: Validación de disponibilidad
        CitaDTO dto = new CitaDTO();
        dto.setVeterinarioId(1L);
        dto.setFechaHora(LocalDateTime.of(2026, 6, 15, 10, 0));

        Veterinario vet = new Veterinario();
        vet.setId(1L);

        when(veterinarioRepository.findById(1L)).thenReturn(Optional.of(vet));
        // Simulamos que ya hay una cita en ese horario:
        when(citaRepository.existsByVeterinarioAndFechaHora(vet, dto.getFechaHora())).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            citaService.agendarCita(dto);
        });

        assertEquals("El veterinario ya tiene una cita agendada en esa fecha y hora exacta.", exception.getMessage());
    }

    @Test
    void testNoPermitirRegistrarCitaSiPacienteNoExiste() {
        CitaDTO dto = new CitaDTO();
        dto.setVeterinarioId(1L);
        dto.setMascotaId(99L); // Mascota que no existe
        dto.setFechaHora(LocalDateTime.of(2026, 6, 15, 10, 0));

        Veterinario vet = new Veterinario();
        vet.setId(1L);

        when(veterinarioRepository.findById(1L)).thenReturn(Optional.of(vet));
        when(citaRepository.existsByVeterinarioAndFechaHora(vet, dto.getFechaHora())).thenReturn(false);
        // Simulamos que no encuentra a la mascota
        when(mascotaRepository.findById(99L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            citaService.agendarCita(dto);
        });

        assertEquals("Mascota no encontrada", exception.getMessage());
    }

    @Test
    void testAgendarCitaExitosamente() {
        CitaDTO dto = new CitaDTO();
        dto.setVeterinarioId(1L);
        dto.setMascotaId(1L);
        dto.setMotivo("Vacunación Anual");
        dto.setFechaHora(LocalDateTime.of(2026, 6, 15, 10, 0));

        Veterinario vet = new Veterinario();
        vet.setId(1L);

        Mascota mascota = new Mascota();
        mascota.setId(1L);

        Cita citaGuardadaMock = new Cita();
        citaGuardadaMock.setMotivo("Vacunación Anual");
        citaGuardadaMock.setMascota(mascota);
        citaGuardadaMock.setVeterinario(vet);

        // Definimos el comportamiento feliz: encuentra todo y guarda
        when(veterinarioRepository.findById(1L)).thenReturn(Optional.of(vet));
        when(citaRepository.existsByVeterinarioAndFechaHora(vet, dto.getFechaHora())).thenReturn(false);
        when(mascotaRepository.findById(1L)).thenReturn(Optional.of(mascota));
        when(citaRepository.save(any(Cita.class))).thenReturn(citaGuardadaMock);

        Cita resultado = citaService.agendarCita(dto);

        assertNotNull(resultado);
        assertEquals("Vacunación Anual", resultado.getMotivo());
    }
}