package com.example.veterinaria.Repository;
import com.example.veterinaria.Model.Rol;
import com.example.veterinaria.Model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario,Long> {
    Optional<Usuario> findByEmail(String email);
    long countByRol(Rol rol);
}
