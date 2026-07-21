package com.example.veterinaria.Repository;
import com.example.veterinaria.Model.HistorialMedico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
public interface HistorialMedicoRepository extends JpaRepository<HistorialMedico, Long> {
    List<HistorialMedico> findByMascotaIdOrderByFechaAtencionDesc(Long mascotaId);
}
