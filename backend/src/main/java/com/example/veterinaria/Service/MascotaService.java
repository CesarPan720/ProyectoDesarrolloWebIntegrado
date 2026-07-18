package com.example.veterinaria.Service;

import com.example.veterinaria.DTO.MascotaDTO;
import com.example.veterinaria.Model.*;
import com.example.veterinaria.Repository.ClienteRepository;
import com.example.veterinaria.Repository.MascotaRepository;
import com.example.veterinaria.Repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.List;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;

    public MascotaService(MascotaRepository mascotaRepository, ClienteRepository clienteRepository, UsuarioRepository usuarioRepository) {
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public List<Mascota> obtenerMascotas(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.CLIENTE) {
            Cliente cliente = clienteRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
            return mascotaRepository.findByCliente(cliente);
        }

        return mascotaRepository.findAll();
    }

    @Transactional
    public Mascota registrarMascota(MascotaDTO dto, String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cliente clienteDueno;

        if (usuario.getRol() == Rol.CLIENTE) {
            clienteDueno = clienteRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
        } else {
            if (dto.getClienteId() == null) {
                throw new IllegalArgumentException("El ID del cliente es obligatorio para el Administrador");
            }
            clienteDueno = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("El cliente especificado no existe"));
        }

        Mascota nueva = new Mascota();
        nueva.setNombre(dto.getNombre());
        nueva.setEspecie(dto.getEspecie());
        nueva.setRaza(dto.getRaza());
        nueva.setPeso(dto.getPeso());
        nueva.setCliente(clienteDueno);

        return mascotaRepository.save(nueva);
    }

    @Transactional
    public Mascota actualizarMascota(Long id, MascotaDTO dto, String email) {
        Mascota mascotaExistente = mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        Cliente clienteDueno;

        if (usuario.getRol() == Rol.CLIENTE) {
            clienteDueno = clienteRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));

            if (!mascotaExistente.getCliente().getId().equals(clienteDueno.getId())) {
                throw new RuntimeException("Error: No tienes permiso para editar esta mascota.");
            }
        } else {
            if (dto.getClienteId() == null) {
                throw new IllegalArgumentException("Error: El dueño es obligatorio.");
            }
            clienteDueno = clienteRepository.findById(dto.getClienteId())
                    .orElseThrow(() -> new RuntimeException("El cliente especificado no existe."));
        }

        mascotaExistente.setNombre(dto.getNombre());
        mascotaExistente.setEspecie(dto.getEspecie());
        mascotaExistente.setRaza(dto.getRaza());
        mascotaExistente.setPeso(dto.getPeso());
        mascotaExistente.setCliente(clienteDueno);

        return mascotaRepository.save(mascotaExistente);
    }

    @Transactional
    public void eliminarMascota(Long id, String email) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (usuario.getRol() == Rol.CLIENTE) {
            Cliente cliente = clienteRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
            if (!mascota.getCliente().getId().equals(cliente.getId())) {
                throw new RuntimeException("No tienes permiso para eliminar esta mascota.");
            }
        }

        mascotaRepository.delete(mascota);
    }


    @Transactional
    public Mascota guardarFoto(Long id, MultipartFile archivo, String email) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mascota no encontrada"));

        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        if (usuario.getRol() == Rol.CLIENTE) {
            Cliente cliente = clienteRepository.findByUsuario(usuario)
                    .orElseThrow(() -> new RuntimeException("Perfil de cliente no encontrado"));
            if (!mascota.getCliente().getId().equals(cliente.getId())) {
                throw new RuntimeException("No tienes permiso para modificar esta mascota.");
            }
        }

        if (archivo.isEmpty()) {
            throw new IllegalArgumentException("El archivo no puede estar vacío");
        }

        try {
            Path directorioUploads = Paths.get("/app/uploads");
            if (!Files.exists(directorioUploads)) {
                Files.createDirectories(directorioUploads);
            }

            String nombreUnicoArchivo = UUID.randomUUID().toString() + "_" + archivo.getOriginalFilename().replace(" ", "_");
            Path rutaCompleta = directorioUploads.resolve(nombreUnicoArchivo);

            Files.copy(archivo.getInputStream(), rutaCompleta);

            if (mascota.getFoto() != null) {
                Path fotoAntigua = directorioUploads.resolve(mascota.getFoto());
                Files.deleteIfExists(fotoAntigua);
            }

            mascota.setFoto(nombreUnicoArchivo);
            return mascotaRepository.save(mascota);

        } catch (IOException e) {
            throw new RuntimeException("Error de Entrada/Salida al guardar la imagen: " + e.getMessage());
        }
    }
}
