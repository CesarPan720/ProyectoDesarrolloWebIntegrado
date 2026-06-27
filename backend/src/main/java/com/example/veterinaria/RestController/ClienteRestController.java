package com.example.veterinaria.RestController;

import com.example.veterinaria.Model.Cliente;
import com.example.veterinaria.Service.ClienteService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/clientes")
@CrossOrigin(origins = "http://localhost:4200")
public class ClienteRestController {

    private final ClienteService clienteService;

    public ClienteRestController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(clienteService.obtenerTodos());
    }

    @PostMapping
    public ResponseEntity<?> crearCliente(@RequestBody Map<String, String> payload) {
        Cliente nuevoCliente = clienteService.crearCliente(payload);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevoCliente);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCliente(@PathVariable Long id, @RequestBody Cliente datosActualizados) {
        Cliente clienteActualizado = clienteService.actualizarCliente(id, datosActualizados);
        return ResponseEntity.ok(clienteActualizado);
    }
}