import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { SidebarComponent } from '../../shared/sidebar/sidebar';

@Component({
selector: 'app-clientes',
standalone: true,
imports: [CommonModule, ReactiveFormsModule, SidebarComponent],
templateUrl: './clientes.html',
styleUrls: ['./clientes.css']
})
export class ClientesComponent implements OnInit {
clientes: any[] = [];
mostrarModal = false;
clienteForm!: FormGroup;
idClienteEditar: number | null = null;

constructor(
    private http: HttpClient,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarClientes();

    this.clienteForm = this.fb.group({
      nombre: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', Validators.required],
      password: [''] // NUEVO: Agregamos contraseña
    });
  }

  cargarClientes() {
    this.http.get<any[]>('http://localhost:8080/api/admin/clientes').subscribe({
      next: (data) => {
        this.clientes = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar clientes', err)
    });
  }

  // NUEVO MÉTODO
  abrirModalNuevo() {
    this.idClienteEditar = null;
    this.clienteForm.reset();
    this.clienteForm.get('email')?.enable();
    this.clienteForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.clienteForm.get('password')?.updateValueAndValidity();
    this.mostrarModal = true;
  }

  abrirModalEdicion(cliente: any) {
    this.idClienteEditar = cliente.id;
    this.clienteForm.patchValue({
      nombre: cliente.nombre,
      email: cliente.email,
      telefono: cliente.telefono,
      password: ''
    });
    this.clienteForm.get('email')?.enable(); // Permitimos editar el correo
    this.clienteForm.get('password')?.clearValidators();
    this.clienteForm.get('password')?.updateValueAndValidity();
    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
    this.idClienteEditar = null;
    this.clienteForm.reset();
  }

  guardarCambios() {
    if (this.clienteForm.valid) {
      if (this.idClienteEditar) {
        // MODO EDICIÓN (PUT)
        this.http.put(`http://localhost:8080/api/admin/clientes/${this.idClienteEditar}`, this.clienteForm.getRawValue()).subscribe({
          next: () => {
            alert('Datos del cliente actualizados con éxito');
            this.cargarClientes();
            this.cerrarModal();
          },
          error: (err) => alert(typeof err.error === 'string' ? err.error : 'Error al actualizar cliente')
        });
      } else {
        // MODO CREACIÓN (POST)
        this.http.post('http://localhost:8080/api/admin/clientes', this.clienteForm.value).subscribe({
          next: () => {
            alert('Cliente registrado con éxito');
            this.cargarClientes();
            this.cerrarModal();
          },
          error: (err) => alert(typeof err.error === 'string' ? err.error : 'Error al crear cliente')
        });
      }
    }
  }
}
