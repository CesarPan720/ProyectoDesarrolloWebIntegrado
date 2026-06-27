package com.example.veterinaria.Repository;

import com.example.veterinaria.Model.Cliente;
import com.example.veterinaria.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente,Long> {
    Optional<Cliente> findByUsuario(Usuario usuario);
}
