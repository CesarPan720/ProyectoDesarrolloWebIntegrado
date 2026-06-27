import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
selector: 'app-citas',
standalone: true,
imports: [CommonModule, ReactiveFormsModule, SidebarComponent],
templateUrl: './citas.html',
styleUrls: ['./citas.css']
})
export class CitasComponent implements OnInit {
userRole: string | null = '';
citas: any[] = [];
misMascotas: any[] = [];
veterinarios: any[] = [];
mostrarModal = false;
citaForm!: FormGroup;

constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.obtenerRol();
    this.cargarCitas();
    this.cargarMascotas();
    this.cargarVeterinarios();

    this.citaForm = this.fb.group({
      mascotaId: ['', Validators.required],
      veterinarioId: ['', Validators.required],
      fechaHora: ['', Validators.required],
      motivo: ['', [Validators.required, Validators.maxLength(200)]]
    });
  }

  cargarCitas() {
    this.http.get<any[]>('http://localhost:8080/api/citas').subscribe({
      next: (data) => {
        this.citas = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar citas', err)
    });
  }

  cargarMascotas() {
    this.http.get<any[]>('http://localhost:8080/api/mascotas').subscribe({
      next: (data) => this.misMascotas = data,
      error: (err) => console.error('Error al cargar tus mascotas', err)
    });
  }

  cargarVeterinarios() {
    this.http.get<any[]>('http://localhost:8080/api/admin/veterinarios').subscribe({
      next: (data) => this.veterinarios = data,
      error: (err) => console.error('Error al cargar veterinarios', err)
    });
  }

  agendarCita() {
    if (this.citaForm.valid) {
      this.http.post('http://localhost:8080/api/citas', this.citaForm.value).subscribe({
        next: () => {
          alert('¡Cita agendada con éxito!');
          this.cargarCitas();
          this.cerrarModal();
        },
        error: (err) => {
          alert(err.error || 'Error: El horario seleccionado no está disponible.');
        }
      });
    }
  }

  cancelarCita(id: number) {
    if (confirm('¿Estás seguro de que deseas cancelar esta cita?')) {
      this.http.put(`http://localhost:8080/api/citas/${id}/estado`, "CANCELADA").subscribe({
        next: () => {
          alert('Cita cancelada');
          this.cargarCitas();
        },
        error: (err) => console.error(err)
      });
    }
  }

  // Método para el Veterinario
  completarCita(id: number) {
    if (confirm('¿Confirmas que ya atendiste a este paciente?')) {
      this.http.put(`http://localhost:8080/api/citas/${id}/estado`, "COMPLETADA").subscribe({
        next: () => {
          alert('¡Excelente! Historial actualizado y cita completada.');
          this.cargarCitas();
        },
        error: (err) => console.error('Error al completar cita', err)
      });
    }
  }

  abrirModal() { this.mostrarModal = true; }
  cerrarModal() {
    this.mostrarModal = false;
    this.citaForm.reset();
  }
}
