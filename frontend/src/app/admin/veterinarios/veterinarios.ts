import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { SidebarComponent } from '../../shared/sidebar/sidebar';

@Component({
selector: 'app-veterinarios',
standalone: true,
imports: [CommonModule, ReactiveFormsModule, SidebarComponent],
templateUrl: './veterinarios.html',
styleUrls: ['./veterinarios.css']
})
export class VeterinariosComponent implements OnInit {
veterinarios: any[] = [];
mostrarModal = false;
vetForm!: FormGroup;
idVetEditar: number | null = null;

constructor(
    private http: HttpClient,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarVeterinarios();

    this.vetForm = this.fb.group({
      nombre: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', Validators.required],
      especialidad: ['', Validators.required],
      licencia: ['', Validators.required],
      password: ['']
    });
  }

  cargarVeterinarios() {
    this.http.get<any[]>('http://localhost:8080/api/admin/veterinarios').subscribe({
      next: (data) => {
        this.veterinarios = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar veterinarios', err)
    });
  }

  abrirModalNuevo() {
    this.idVetEditar = null;
    this.vetForm.reset();
    this.vetForm.get('email')?.enable();
    this.vetForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.vetForm.get('password')?.updateValueAndValidity();
    this.mostrarModal = true;
  }

  abrirModalEdicion(vet: any) {
    this.idVetEditar = vet.id;
    this.vetForm.patchValue({
      nombre: vet.nombre,
      email: vet.email,
      telefono: vet.telefono,
      especialidad: vet.especialidad,
      licencia: vet.licencia,
      password: ''
    });
    this.vetForm.get('email')?.disable(); // No permitimos cambiar correo al editar por ahora
    this.vetForm.get('password')?.clearValidators();
    this.vetForm.get('password')?.updateValueAndValidity();
    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
    this.idVetEditar = null;
    this.vetForm.reset();
  }

  guardarCambios() {
    if (this.vetForm.valid) {
      if (this.idVetEditar) {
        this.http.put(`http://localhost:8080/api/admin/veterinarios/${this.idVetEditar}`, this.vetForm.getRawValue()).subscribe({
          next: () => {
            alert('Veterinario actualizado con éxito');
            this.cargarVeterinarios();
            this.cerrarModal();
          },
          error: (err) => alert('Error al actualizar')
        });
      } else {
        this.http.post('http://localhost:8080/api/admin/veterinarios', this.vetForm.value).subscribe({
          next: () => {
            alert('Veterinario registrado con éxito');
            this.cargarVeterinarios();
            this.cerrarModal();
          },
          error: (err) => {
            alert('Error: ' + (typeof err.error === 'string' ? err.error : 'Verifica los datos'));
          }
        });
      }
    }
  }
}
