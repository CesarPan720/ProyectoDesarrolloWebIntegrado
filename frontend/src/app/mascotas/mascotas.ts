import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
selector: 'app-mascotas',
standalone: true,
imports: [CommonModule, ReactiveFormsModule, SidebarComponent],
templateUrl: './mascotas.html',
styleUrls: ['./mascotas.css']
})
export class MascotasComponent implements OnInit {
userRole: string | null = '';
mostrarModal = false;
mascotaForm!: FormGroup;

archivoSeleccionado: File | null = null;

mascotas: any[] = [];
clientes: any[] = []; // <-- Lista para almacenar los dueños (desplegable)
idMascotaEditar: number | null = null; // <-- Rastrea si estamos editando

constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.obtenerRol();
    this.cargarMascotas();

    // Si es ADMIN, cargamos la lista de clientes para el desplegable
    if (this.userRole === 'ADMIN') {
      this.cargarClientes();
    }

    this.mascotaForm = this.fb.group({
      nombre: ['', Validators.required],
      especie: ['Perro', Validators.required],
      raza: ['', Validators.required],
      peso: [0, [Validators.required, Validators.min(0.1)]],
      clienteId: ['']
    });
  }

  cargarMascotas() {
    if (this.userRole === 'VETERINARIO') {
      // Si es Veterinario, extraemos a sus pacientes desde su historial de citas
      this.http.get<any[]>('http://localhost:8080/api/citas').subscribe({
        next: (data) => {
          const mascotasMap = new Map();
          data.forEach(cita => {
            // Guardamos la mascota, pero además le agregamos el nombre del dueño para que pueda verlo en la tabla
            if (cita.mascota && !mascotasMap.has(cita.mascota.id)) {
              // Validamos si la mascota trae la info del cliente incrustada
              mascotasMap.set(cita.mascota.id, cita.mascota);
            }
          });
          this.mascotas = Array.from(mascotasMap.values());
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error al cargar pacientes del veterinario', err)
      });

    } else {
      // Si es CLIENTE o ADMIN, usamos el endpoint directo normal
      this.http.get<any[]>('http://localhost:8080/api/mascotas').subscribe({
        next: (data) => {
          this.mascotas = data;
          this.cdr.detectChanges();
        },
        error: (err) => console.error('Error al cargar mascotas', err)
      });
    }
  }

  cargarClientes() {
    // Consumimos tu endpoint existente de clientes
    this.http.get<any[]>('http://localhost:8080/api/admin/clientes').subscribe({
      next: (data) => {
        this.clientes = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar clientes para el combo', err)
    });
  }

  guardarMascota() {
    if (this.mascotaForm.valid) {
      const payload = { ...this.mascotaForm.value };
      payload.peso = parseFloat(payload.peso);

      if (this.userRole !== 'ADMIN') {
        delete payload.clienteId;
      } else {
        if (!payload.clienteId || payload.clienteId === '') {
          delete payload.clienteId;
        } else {
          payload.clienteId = parseInt(payload.clienteId, 10);
        }
      }

      if (this.idMascotaEditar) {
        this.http.put(`http://localhost:8080/api/mascotas/${this.idMascotaEditar}`, payload).subscribe({
          next: (res: any) => {
            // Si hay archivo seleccionado, lo subimos inmediatamente
            if (this.archivoSeleccionado) {
              this.subirImagenServidor(res.id);
            } else {
              alert('¡Mascota actualizada con éxito!');
              this.cargarMascotas();
              this.cerrarModal();
            }
          },
          error: (err) => alert('Error al actualizar la mascota')
        });
      } else {
        this.http.post('http://localhost:8080/api/mascotas', payload).subscribe({
          next: (res: any) => {
            if (this.archivoSeleccionado) {
              this.subirImagenServidor(res.id);
            } else {
              alert('¡Mascota registrada con éxito!');
              this.cargarMascotas();
              this.cerrarModal();
            }
          },
          error: (err) => alert('Error al registrar la mascota.')
        });
      }
    }
  }

  eliminarMascota(id: number) {
    if (confirm('¿Estás seguro de que deseas eliminar esta mascota?')) {
      this.http.delete(`http://localhost:8080/api/mascotas/${id}`).subscribe({
        next: () => {
          alert('Mascota eliminada correctamente');
          this.cargarMascotas();
        },
        error: (err) => console.error('Error al eliminar', err)
      });
    }
  }

  abrirModal(mascota?: any) {
    this.mostrarModal = true;

    if (mascota) {
      // Si recibimos una mascota por parámetro, cambiamos a Modo Edición
      this.idMascotaEditar = mascota.id;
      this.mascotaForm.patchValue({
        nombre: mascota.nombre,
        especie: mascota.especie,
        raza: mascota.raza,
        peso: mascota.peso,
        clienteId: mascota.cliente ? mascota.cliente.id : ''
      });
    } else {
      // Modo Registro Nuevo
      this.idMascotaEditar = null;
      this.mascotaForm.reset({ especie: 'Perro', peso: 0, clienteId: '' });
    }
  }

  cerrarModal() {
    this.mostrarModal = false;
    this.idMascotaEditar = null;
    this.archivoSeleccionado = null;
    this.mascotaForm.reset({ especie: 'Perro', peso: 0, clienteId: '' });
  }

  puedeEditar(): boolean {
    return this.userRole !== 'VETERINARIO';
  }

 onFileSelected(event: any) {
  const input = event.target;
  if (input && input.files && input.files.length > 0) {
    this.archivoSeleccionado = input.files[0];
  } else {
    this.archivoSeleccionado = null;
  }
}

  // ==== NUEVO MÉTODO: SUBIR ARCHIVO MEDIANTE FORMDATA ====
  subirImagenServidor(mascotaId: number) {
    if (!this.archivoSeleccionado) return;

    const formData = new FormData();
    formData.append('archivo', this.archivoSeleccionado);

    this.http.post(`http://localhost:8080/api/mascotas/${mascotaId}/foto`, formData).subscribe({
      next: () => {
        alert('¡Mascota guardada y foto subida correctamente!');
        this.archivoSeleccionado = null;
        this.cargarMascotas();
        this.cerrarModal();
      },
      error: (err) => {
        alert('Se guardó la mascota pero la foto falló al subir.');
        this.cargarMascotas();
        this.cerrarModal();
      }
    });
  }

  




}
