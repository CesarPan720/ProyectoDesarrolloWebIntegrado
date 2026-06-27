package com.example.veterinaria.Service;

import com.example.veterinaria.Model.Cliente;
import com.example.veterinaria.Model.Rol;
import com.example.veterinaria.Model.Usuario;
import com.example.veterinaria.Model.Veterinario;
import com.example.veterinaria.Repository.ClienteRepository;
import com.example.veterinaria.Repository.UsuarioRepository;
import com.example.veterinaria.Repository.VeterinarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClienteRepository clienteRepository;
    private final VeterinarioRepository veterinarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                          ClienteRepository clienteRepository, VeterinarioRepository veterinarioRepository) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.clienteRepository = clienteRepository;
        this.veterinarioRepository = veterinarioRepository;
    }

    public List<Usuario> obtenerTodos() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario crearUsuario(Map<String, String> payload) {
        String email = payload.get("email");

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Error: El correo electrónico ya está registrado.");
        }

        Usuario u = new Usuario();
        u.setNombreCompleto(payload.get("nombreCompleto"));
        u.setEmail(email);
        u.setTelefono(payload.get("telefono"));
        u.setPassword(passwordEncoder.encode(payload.get("password")));
        u.setRol(Rol.valueOf(payload.get("rol").toUpperCase()));
        u.setActivo(true);
        Usuario usuarioGuardado = usuarioRepository.save(u);

        if (u.getRol() == Rol.CLIENTE) {
            Cliente c = new Cliente();
            c.setNombre(u.getNombreCompleto());
            c.setEmail(u.getEmail());
            c.setTelefono(u.getTelefono());
            c.setUsuario(usuarioGuardado);
            clienteRepository.save(c);

        } else if (u.getRol() == Rol.VETERINARIO) {
            Veterinario v = new Veterinario();
            v.setNombre(u.getNombreCompleto());
            v.setEmail(u.getEmail());
            v.setTelefono(u.getTelefono());
            v.setEspecialidad("Por asignar");
            v.setLicencia("Por asignar");
            v.setUsuario(usuarioGuardado);
            veterinarioRepository.save(v);
        }

        return usuarioGuardado;
    }

    @Transactional
    public Usuario actualizarUsuario(Long id, Usuario usuarioActualizado) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!usuario.getEmail().equals(usuarioActualizado.getEmail())) {
            if (usuarioRepository.findByEmail(usuarioActualizado.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Error: El correo electrónico ya está registrado por otro usuario.");
            }
            usuario.setEmail(usuarioActualizado.getEmail());
        }

        usuario.setNombreCompleto(usuarioActualizado.getNombreCompleto());
        usuario.setTelefono(usuarioActualizado.getTelefono());
        usuario.setRol(usuarioActualizado.getRol());

        if (usuarioActualizado.getPassword() != null && !usuarioActualizado.getPassword().isEmpty()) {
            usuario.setPassword(passwordEncoder.encode(usuarioActualizado.getPassword()));
        }

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario cambiarEstado(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        usuario.setActivo(!usuario.getActivo());
        return usuarioRepository.save(usuario);
    }
}