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

        // 1. Armamos el nombre completo concatenando para la credencial de acceso
        String nombreCompleto = payload.get("nombres") + " " + payload.get("apellidos");

        Usuario u = new Usuario();
        u.setNombreCompleto(nombreCompleto); // Usamos la concatenación
        u.setEmail(email);
        u.setTelefono(payload.get("telefono"));
        u.setPassword(passwordEncoder.encode(payload.get("password")));
        u.setRol(Rol.CLIENTE);
        u.setActivo(true);
        Usuario usuarioGuardado = usuarioRepository.save(u);

        // 2. Creamos el perfil físico con los nuevos campos de ApiPeru
        Cliente c = new Cliente();
        c.setDni(payload.get("dni"));               // NUEVO
        c.setNombres(payload.get("nombres"));       // NUEVO
        c.setApellidos(payload.get("apellidos"));   // NUEVO
        c.setNombre(nombreCompleto);                // Mantenido por compatibilidad
        c.setEmail(u.getEmail());
        c.setTelefono(u.getTelefono());
        c.setUsuario(usuarioGuardado);

        return clienteRepository.save(c);
    }

    @Transactional
    public Cliente actualizarCliente(Long id, Cliente datosActualizados) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Actualizamos los nuevos campos
        cliente.setDni(datosActualizados.getDni());
        cliente.setNombres(datosActualizados.getNombres());
        cliente.setApellidos(datosActualizados.getApellidos());
        cliente.setTelefono(datosActualizados.getTelefono());
        
        // Volvemos a concatenar por si le corrigieron un apellido o nombre
        String nombreCompletoActualizado = datosActualizados.getNombres() + " " + datosActualizados.getApellidos();
        cliente.setNombre(nombreCompletoActualizado); 

        // Validamos si cambió el correo
        if (!cliente.getEmail().equals(datosActualizados.getEmail())) {
            if (usuarioRepository.findByEmail(datosActualizados.getEmail()).isPresent()) {
                throw new IllegalArgumentException("Error: El correo ya está registrado por otro usuario.");
            }
            cliente.setEmail(datosActualizados.getEmail());
        }

        // Sincronizamos la credencial (Usuario) para que tenga el mismo nombre corregido
        Usuario usuarioAsociado = cliente.getUsuario();
        if (usuarioAsociado != null) {
            usuarioAsociado.setNombreCompleto(nombreCompletoActualizado);
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