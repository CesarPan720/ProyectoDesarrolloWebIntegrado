package com.example.veterinaria.Repository;
import com.example.veterinaria.Model.Cita;
import com.example.veterinaria.Model.Mascota;
import com.example.veterinaria.Model.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita,Long> {
    List<Cita> findByVeterinario(Veterinario veterinario);

    @Query("SELECT c FROM Cita c WHERE c.mascota IN :mascotas")
    List<Cita> findByMascotaIn(@Param("mascotas") List<Mascota> mascotas);

    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Cita c WHERE c.veterinario = :veterinario AND c.fechaHora = :fechaHora")
    boolean existsByVeterinarioAndFechaHora(@Param("veterinario") Veterinario veterinario, @Param("fechaHora") LocalDateTime fechaHora);
}
