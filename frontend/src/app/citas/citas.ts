import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth';
import { SidebarComponent } from '../shared/sidebar/sidebar';
import { DiagnosticoService } from './diagnostico.service';
import { Diagnostico } from './diagnostico.model';

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
  
  // Modales
  mostrarModal = false;
  mostrarModalDiagnostico = false;
  
  // Formularios
  citaForm!: FormGroup;
  diagnosticoForm!: FormGroup;

  idCitaSeleccionada!: number;

  constructor(
    private authService: AuthService,
    private fb: FormBuilder,
    private http: HttpClient,
    private cdr: ChangeDetectorRef,
    private diagnosticoService: DiagnosticoService
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

    this.diagnosticoForm = this.fb.group({
      descripcion: ['', [Validators.required, Validators.minLength(10)]],
      recetaMedica: ['', Validators.required]
    });
  }

  cargarCitas() {
    this.http.get<any[]>('http://localhost:8080/api/citas').subscribe({
      next: (data) => {
        this.citas = data;
        this.cdr.detectChanges();
      },
      error: (err: any) => console.error('Error al cargar citas', err)
    });
  }

  cargarMascotas() {
    this.http.get<any[]>('http://localhost:8080/api/mascotas').subscribe({
      next: (data) => this.misMascotas = data,
      error: (err: any) => console.error('Error al cargar tus mascotas', err)
    });
  }

  cargarVeterinarios() {
    this.http.get<any[]>('http://localhost:8080/api/admin/veterinarios').subscribe({
      next: (data) => this.veterinarios = data,
      error: (err: any) => console.error('Error al cargar veterinarios', err)
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
        error: (err: any) => {
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
        error: (err: any) => console.error(err)
      });
    }
  }

  completarCita(id: number) {
    this.idCitaSeleccionada = id;
    this.mostrarModalDiagnostico = true;
  }

  guardarDiagnosticoYCompletar() {
    if (this.diagnosticoForm.valid) {
      const nuevoDiagnostico: Diagnostico = this.diagnosticoForm.value;

      this.diagnosticoService.registrarDiagnostico(this.idCitaSeleccionada, nuevoDiagnostico).subscribe({
        next: () => {
          this.http.put(`http://localhost:8080/api/citas/${this.idCitaSeleccionada}/estado`, "COMPLETADA").subscribe({
            next: () => {
              alert('¡Excelente! Diagnóstico registrado y cita completada con éxito.');
              this.cargarCitas();
              this.cerrarModalDiagnostico();
            },
            error: (err: any) => {
              console.error('Error al cambiar el estado de la cita', err);
              alert('Se guardó el diagnóstico, pero no se pudo actualizar el estado de la cita.');
            }
          });
        },
        error: (err: any) => {
          console.error('Error al guardar el diagnóstico', err);
          alert('Hubo un error al guardar el diagnóstico.');
        }
      });
    }
  }

  abrirModal() { this.mostrarModal = true; }
  
  cerrarModal() {
    this.mostrarModal = false;
    this.citaForm.reset();
  }

  cerrarModalDiagnostico() {
    this.mostrarModalDiagnostico = false;
    this.diagnosticoForm.reset();
  }
}