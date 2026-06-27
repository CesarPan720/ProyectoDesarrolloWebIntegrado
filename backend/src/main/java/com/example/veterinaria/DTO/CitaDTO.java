package com.example.veterinaria.DTO;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class CitaDTO {
    private Long mascotaId;
    private Long veterinarioId;
    private LocalDateTime fechaHora;
    private String motivo;
}
