package com.example.veterinaria.Service;

import com.example.veterinaria.DTO.RegistroDTO;
import com.example.veterinaria.Model.Cliente;
import com.example.veterinaria.Model.Rol;
import com.example.veterinaria.Model.Usuario;
import com.example.veterinaria.Model.Veterinario;
import com.example.veterinaria.Repository.ClienteRepository;
import com.example.veterinaria.Repository.UsuarioRepository;
import com.example.veterinaria.Repository.VeterinarioRepository;
import com.example.veterinaria.Security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final ClienteRepository clienteRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UsuarioRepository usuarioRepository, ClienteRepository clienteRepository,
                       VeterinarioRepository veterinarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.clienteRepository = clienteRepository;
        this.veterinarioRepository = veterinarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Transactional
    public Usuario registrar(RegistroDTO dto) {
        if (usuarioRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombreCompleto(dto.getNombreCompleto());
        nuevoUsuario.setEmail(dto.getEmail());
        nuevoUsuario.setTelefono(dto.getTelefono());
        nuevoUsuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        nuevoUsuario.setRol(Rol.valueOf(dto.getTipoUsuario().toUpperCase()));
        nuevoUsuario.setActivo(true);

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        if (usuarioGuardado.getRol() == Rol.CLIENTE) {
            Cliente c = new Cliente();
            c.setNombre(usuarioGuardado.getNombreCompleto());
            c.setEmail(usuarioGuardado.getEmail());
            c.setTelefono(usuarioGuardado.getTelefono());
            c.setUsuario(usuarioGuardado);
            clienteRepository.save(c);
        } else if (usuarioGuardado.getRol() == Rol.VETERINARIO) {
            Veterinario v = new Veterinario();
            v.setNombre(usuarioGuardado.getNombreCompleto());
            v.setEmail(usuarioGuardado.getEmail());
            v.setTelefono(usuarioGuardado.getTelefono());
            v.setEspecialidad("Por asignar");
            v.setLicencia("Por asignar");
            v.setUsuario(usuarioGuardado);
            veterinarioRepository.save(v);
        }
        return usuarioGuardado;
    }

    public String login(String email, String rawPassword) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales incorrectas"));

        if (!passwordEncoder.matches(rawPassword, usuario.getPassword())) {
            throw new IllegalArgumentException("Credenciales incorrectas");
        }

        if (!usuario.getActivo()) {
            throw new IllegalArgumentException("Su cuenta ha sido desactivada. Contacte al administrador.");
        }

        return jwtUtil.generarToken(usuario.getEmail(), usuario.getRol().name());
    }
}