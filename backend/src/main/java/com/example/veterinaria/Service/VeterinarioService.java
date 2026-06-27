package com.example.veterinaria.Service;

import com.example.veterinaria.DTO.VeterinarioDTO;
import com.example.veterinaria.Model.Rol;
import com.example.veterinaria.Model.Usuario;
import com.example.veterinaria.Model.Veterinario;
import com.example.veterinaria.Repository.UsuarioRepository;
import com.example.veterinaria.Repository.VeterinarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public VeterinarioService(VeterinarioRepository veterinarioRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.veterinarioRepository = veterinarioRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Veterinario> obtenerTodos() {
        return veterinarioRepository.findAll();
    }

    @Transactional
    public Veterinario crearVeterinario(VeterinarioDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado.");
        }

        Usuario u = new Usuario();
        u.setNombreCompleto(dto.getNombre());
        u.setEmail(dto.getEmail());
        u.setTelefono(dto.getTelefono());
        u.setPassword(passwordEncoder.encode(dto.getPassword()));
        u.setRol(Rol.VETERINARIO);
        u.setActivo(true);
        usuarioRepository.save(u);

        Veterinario v = new Veterinario();
        v.setNombre(dto.getNombre());
        v.setEmail(dto.getEmail());
        v.setTelefono(dto.getTelefono());
        v.setEspecialidad(dto.getEspecialidad());
        v.setLicencia(dto.getLicencia());
        v.setUsuario(u);

        return veterinarioRepository.save(v);
    }

    @Transactional
    public Veterinario actualizarVeterinario(Long id, VeterinarioDTO dto) {
        Veterinario v = veterinarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Veterinario no encontrado"));

        v.setNombre(dto.getNombre());
        v.setEspecialidad(dto.getEspecialidad());
        v.setLicencia(dto.getLicencia());
        v.setTelefono(dto.getTelefono());

        Usuario u = v.getUsuario();
        u.setNombreCompleto(dto.getNombre());
        u.setTelefono(dto.getTelefono());

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            u.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        usuarioRepository.save(u);
        return veterinarioRepository.save(v);
    }
}
