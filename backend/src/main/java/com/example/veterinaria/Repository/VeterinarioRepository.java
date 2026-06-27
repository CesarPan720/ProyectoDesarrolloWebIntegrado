package com.example.veterinaria.Repository;
import com.example.veterinaria.Model.Usuario;
import com.example.veterinaria.Model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VeterinarioRepository extends JpaRepository<Veterinario,Long> {
    Optional<Veterinario> findByUsuario(Usuario usuario);
}
