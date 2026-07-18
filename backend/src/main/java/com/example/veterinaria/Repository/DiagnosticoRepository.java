package com.example.veterinaria.Repository;
import com.example.veterinaria.Model.Diagnostico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface DiagnosticoRepository extends JpaRepository<Diagnostico, Long> {
    Optional<Diagnostico> findByCitaId(Long citaId);

}
