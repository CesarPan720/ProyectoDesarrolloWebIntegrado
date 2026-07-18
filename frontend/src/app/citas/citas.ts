import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth';
import { SidebarComponent } from '../shared/sidebar/sidebar';
import { DiagnosticoService } from './diagnostico.service';
import { Diagnostico } from './diagnostico.model';

// ==== LIBRERÍAS PARA EL PDF ====
import jsPDF from 'jspdf';
import html2canvas from 'html2canvas';

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
  mostrarModalVerDiagnostico = false; 
  
  // Formularios y datos
  citaForm!: FormGroup;
  diagnosticoForm!: FormGroup;
  idCitaSeleccionada!: number;
  
  diagnosticoDetalle: Diagnostico | null = null; 

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

  // ==== NUEVO MÉTODO INTELIGENTE: PIDE LOS DATOS DEL BACKEND ANTES DE EXPORTAR EL PDF ====
  generarRecetaPDF(cita: any) {
    // Consultamos el servicio de Romina para traer la descripción y receta médica real guardada
    this.diagnosticoService.obtenerDiagnosticoPorCita(cita.id).subscribe({
      next: (diagReal) => {
        const elementoReceta = document.createElement('div');
        elementoReceta.style.padding = '40px';
        elementoReceta.style.width = '700px';
        elementoReceta.style.fontFamily = 'Arial, sans-serif';
        elementoReceta.style.color = '#333333';
        elementoReceta.style.background = '#ffffff';

        elementoReceta.innerHTML = `
          <div style="display: flex; justify-content: space-between; align-items: center; border-bottom: 3px solid #7f5af0; padding-bottom: 20px; margin-bottom: 30px;">
            <div>
              <h1 style="margin: 0; color: #7f5af0; font-size: 26px; font-weight: bold;">🐾 PetCare Clínica Veterinaria</h1>
              <p style="margin: 5px 0 0 0; color: #718096; font-size: 14px;">Salud y bienestar para tu mejor amigo</p>
            </div>
            <div style="text-align: right; color: #4a5568; font-size: 13px; line-height: 1.5;">
              <strong>Fecha:</strong> ${new Date(cita.fechaHora).toLocaleDateString()}<br>
              <strong>Receta N°:</strong> REC-${cita.id}
            </div>
          </div>

          <div style="background: #f7fafc; border-radius: 8px; padding: 20px; margin-bottom: 30px; display: grid; grid-template-columns: 1fr 1fr; gap: 20px; border: 1px solid #e2e8f0;">
            <div>
              <h3 style="margin: 0 0 10px 0; color: #2d3748; border-bottom: 1px solid #cbd5e0; padding-bottom: 5px; font-size: 16px;">Datos del Paciente</h3>
              <p style="margin: 6px 0; font-size: 14px;"><strong>Nombre:</strong> ${cita.mascota?.nombre || '---'}</p>
              <p style="margin: 6px 0; font-size: 14px;"><strong>Especie:</strong> ${cita.mascota?.especie || '---'}</p>
            </div>
            <div>
              <h3 style="margin: 0 0 10px 0; color: #2d3748; border-bottom: 1px solid #cbd5e0; padding-bottom: 5px; font-size: 16px;">Responsables</h3>
              <p style="margin: 6px 0; font-size: 14px;"><strong>Dueño:</strong> ${cita.mascota?.cliente?.nombre || '---'}</p>
              <p style="margin: 6px 0; font-size: 14px;"><strong>Veterinario:</strong> Dr/a. ${cita.veterinario?.nombre || '---'}</p>
            </div>
          </div>

          <div style="margin-bottom: 30px;">
            <h3 style="color: #7f5af0; border-bottom: 2px solid #e2e8f0; padding-bottom: 5px; margin-bottom: 12px; font-size: 16px;">📋 Diagnóstico Médico / Hallazgos</h3>
            <div style="background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; padding: 15px; min-height: 60px; font-size: 14px; line-height: 1.6; color: #4a5568; white-space: pre-wrap;">
              ${diagReal.descripcion || 'No se registró un diagnóstico detallado.'}
            </div>
          </div>

          <div style="margin-bottom: 60px;">
            <h3 style="color: #7f5af0; border-bottom: 2px solid #e2e8f0; padding-bottom: 5px; margin-bottom: 12px; font-size: 16px;">💊 Receta Médica / Tratamiento Obligatorio</h3>
            <div style="background: #fff; border: 1px solid #e2e8f0; border-radius: 6px; padding: 15px; min-height: 100px; font-size: 14px; line-height: 1.6; color: #4a5568; white-space: pre-line;">
              ${diagReal.recetaMedica || 'No se prescribieron medicamentos específicos.'}
            </div>
          </div>

          <div style="margin-top: 120px; display: flex; justify-content: center;">
            <div style="text-align: center; width: 250px; border-top: 1px solid #a0aec0; padding-top: 8px;">
              <p style="margin: 0; font-size: 13px; font-weight: bold; color: #4a5568;">Firma del Médico Veterinario</p>
              <p style="margin: 2px 0 0 0; font-size: 11px; color: #718096;">Clínica Veterinaria PetCare</p>
            </div>
          </div>
        `;

        document.body.appendChild(elementoReceta);

        html2canvas(elementoReceta, { scale: 2 }).then((canvas) => {
          const imgData = canvas.toDataURL('image/png');
          const pdf = new jsPDF('p', 'mm', 'a4');
          const imgWidth = 210;
          const imgHeight = (canvas.height * imgWidth) / canvas.width;

          pdf.addImage(imgData, 'PNG', 0, 0, imgWidth, imgHeight);
          pdf.save(`Receta_${cita.mascota?.nombre || 'Mascota'}_Cita_${cita.id}.pdf`);

          document.body.removeChild(elementoReceta);
        }).catch(err => {
          console.error('Error en renderizado canvas:', err);
          if (document.body.contains(elementoReceta)) document.body.removeChild(elementoReceta);
        });
      },
      error: (err) => {
        console.error('Error al obtener datos médicos para el PDF:', err);
        alert('No se pudo descargar la receta porque aún no hay un diagnóstico guardado en el servidor para esta cita.');
      }
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

  verDiagnostico(citaId: number) {
    this.diagnosticoService.obtenerDiagnosticoPorCita(citaId).subscribe({
      next: (data) => {
        this.diagnosticoDetalle = data;
        this.mostrarModalVerDiagnostico = true;
      },
      error: (err: any) => {
        console.error('Error al consultar diagnóstico', err);
        alert('No se pudo encontrar un diagnóstico registrado para esta cita.');
      }
    });
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

  cerrarModalVerDiagnostico() {
    this.mostrarModalVerDiagnostico = false;
    this.diagnosticoDetalle = null;
  }
}