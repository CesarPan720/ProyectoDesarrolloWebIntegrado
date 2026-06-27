package com.example.veterinaria.Repository;
import com.example.veterinaria.Model.Cliente;
import com.example.veterinaria.Model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MascotaRepository extends JpaRepository<Mascota,Long> {
    List<Mascota> findByCliente(Cliente cliente);
}
