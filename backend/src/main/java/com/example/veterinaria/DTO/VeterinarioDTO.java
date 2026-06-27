package com.example.veterinaria.DTO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VeterinarioDTO {
    private String nombre;
    private String email;
    private String telefono;
    private String password;
    private String especialidad;
    private String licencia;
}
