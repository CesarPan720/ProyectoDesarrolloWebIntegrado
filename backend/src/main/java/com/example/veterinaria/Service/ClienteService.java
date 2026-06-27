package com.example.veterinaria.Service;

import com.example.veterinaria.Model.Cliente;
import com.example.veterinaria.Model.Rol;
import com.example.veterinaria.Model.Usuario;
import com.example.veterinaria.Repository.ClienteRepository;
import com.example.veterinaria.Repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Cliente> obtenerTodos() {
        return clienteRepository.findAll();
    }

    @Transactional
    public Cliente crearCliente(Map<String, String> payload) {
        String email = payload.get("email");

        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Error: El correo electrónico ya está registrado.");
        }

        Usuario u = new Usuario();
        u.setNombreCompleto(payload.get("nombre"));
        u.setEmail(email);
        u.setTelefono(payload.get("telefono"));
        u.setPassword(passwordEncoder.encode(payload.get("password")));
        u.setRol(Rol.CLIENTE);
        u.setActivo(true);
        Usuario usuarioGuardado = usuarioRepository.save(u);

        Cliente c = new Cliente();
        c.setNombre(u.getNombreCompleto());
        c.setEmail(u.getEmail());
        c.setTelefono(u.getTelefono());
        c.setUsuario(usuarioGuardado);

        return clienteRepository.save(c);
    }

    @Transactional
    public Cliente actualizarCliente(Long id, Cliente datosActualizados) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        cliente.setNombre(datosActualizados.getNombre());
        cliente.setTelefono(datosActualizados.getTelefono());

        if (!cliente.getEmail().equals(datosActualizados.getEmail())) {
            if (usuarioRepository.findByEmail(datosActualizados.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Error: El correo ya está registrado por otro usuario.");
            }
            cliente.setEmail(datosActualizados.getEmail());
        }

        Usuario usuarioAsociado = cliente.getUsuario();
        if (usuarioAsociado != null) {
            usuarioAsociado.setNombreCompleto(datosActualizados.getNombre());
            usuarioAsociado.setEmail(datosActualizados.getEmail());
            usuarioAsociado.setTelefono(datosActualizados.getTelefono());
            usuarioRepository.save(usuarioAsociado);
        }

        return clienteRepository.save(cliente);
    }

    public void eliminarCliente(Long id){
        clienteRepository.deleteById(id);
    }
}