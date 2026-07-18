package com.example.veterinaria.RestController;

import com.example.veterinaria.DTO.MascotaDTO;
import com.example.veterinaria.Model.Mascota;
import com.example.veterinaria.Service.MascotaService;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.nio.file.Path;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import java.net.MalformedURLException;
import java.nio.file.Paths;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;


@RestController
@RequestMapping("/api/mascotas")
@CrossOrigin(origins = "http://localhost:4200")
public class MascotaRestController {

    private final MascotaService mascotaService;
    private final Path rootFolder = Paths.get("/app/uploads");

    public MascotaRestController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping
    public ResponseEntity<List<Mascota>> obtenerMascotas(Authentication auth) {
        return ResponseEntity.ok(mascotaService.obtenerMascotas(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<?> registrarMascota(@Valid @RequestBody MascotaDTO dto, Authentication auth) {
        Mascota nueva = mascotaService.registrarMascota(dto, auth.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarMascota(@PathVariable Long id, @Valid @RequestBody MascotaDTO dto, Authentication auth) {
        Mascota actualizada = mascotaService.actualizarMascota(id, dto, auth.getName());
        return ResponseEntity.ok(actualizada);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarMascota(@PathVariable Long id, Authentication auth) {
        mascotaService.eliminarMascota(id, auth.getName());
        return ResponseEntity.ok(Collections.singletonMap("mensaje", "Mascota eliminada correctamente"));
    }


    @PostMapping("/{id}/foto")
    public ResponseEntity<?> subirFoto(@PathVariable Long id, @RequestParam("archivo") MultipartFile archivo, Authentication auth){
        try {
            Mascota mascota = mascotaService.guardarFoto(id,archivo,auth.getName());
            return ResponseEntity.ok(mascota); 
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Collections.singletonMap("error","No se puede subir la iamgen" + e.getMessage()));
        }
    }

    @GetMapping("/uploads/{filename:.+}")
    public ResponseEntity<Resource> verFoto(@PathVariable String filename) {
        try {
            Path file = rootFolder.resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.IMAGE_JPEG_VALUE)
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.badRequest().build();
        }
    }

}