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

  // NUEVAS VARIABLES PARA LA BÚSQUEDA DE DNI
  buscandoDni = false;
  mensajeErrorDni = '';

  constructor(
    private http: HttpClient,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarClientes();

    this.clienteForm = this.fb.group({
      dni: ['', [Validators.minLength(8), Validators.maxLength(8)]], 
      nombres: ['', Validators.required], 
      apellidos: ['', Validators.required], 
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', Validators.required],
      password: [''] 
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

  // NUEVA FUNCIÓN: Consulta el DNI a través de nuestro backend seguro
  buscarDni() {
    const dni = this.clienteForm.get('dni')?.value;
    
    if (dni && dni.length === 8) {
      this.buscandoDni = true;
      this.mensajeErrorDni = '';

      this.http.get<any>(`http://localhost:8080/api/auth/buscar-dni/${dni}`).subscribe({
        next: (res) => {
          this.buscandoDni = false;
          console.log('Respuesta de la API:', res); 

          if (res && res.dni) {
            this.clienteForm.patchValue({
              nombres: res.nombres,
              apellidos: `${res.apellido_paterno || ''} ${res.apellido_materno || ''}`.trim()
            });
          } else {
            // Cambiamos el mensaje por una advertencia informativa, sin bloquear al usuario
            this.mensajeErrorDni = 'DNI no encontrado. Por favor, ingrese los datos manualmente.';
          }
          this.cdr.detectChanges();
        },
        error: (err) => {
          this.buscandoDni = false;
          this.mensajeErrorDni = 'No se pudo autocompletar. Proceda con el registro manual.';
          this.cdr.detectChanges();
        }
      });
    }
  }

  abrirModalNuevo() {
    this.idClienteEditar = null;
    this.clienteForm.reset();
    this.mensajeErrorDni = '';
    this.clienteForm.get('email')?.enable();
    this.clienteForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.clienteForm.get('password')?.updateValueAndValidity();
    this.mostrarModal = true;
  }

  abrirModalEdicion(cliente: any) {
    this.idClienteEditar = cliente.id;
    this.mensajeErrorDni = '';
    
    // Al editar, parchamos los nuevos campos que vienen de la BD
    this.clienteForm.patchValue({
      dni: cliente.dni,
      nombres: cliente.nombres,
      apellidos: cliente.apellidos,
      email: cliente.email,
      telefono: cliente.telefono,
      password: ''
    });
    
    this.clienteForm.get('email')?.enable(); 
    this.clienteForm.get('password')?.clearValidators();
    this.clienteForm.get('password')?.updateValueAndValidity();
    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
    this.idClienteEditar = null;
    this.clienteForm.reset();
    this.mensajeErrorDni = '';
  }

  guardarCambios() {
    if (this.clienteForm.valid) {
      
      // CRÍTICO: Usamos getRawValue() en lugar de .value para extraer nombres y apellidos 
      // a pesar de que los campos estén en modo 'disabled' (bloqueados).
      const payload = this.clienteForm.getRawValue();

      if (this.idClienteEditar) {
        // MODO EDICIÓN (PUT)
        this.http.put(`http://localhost:8080/api/admin/clientes/${this.idClienteEditar}`, payload).subscribe({
          next: () => {
            alert('Datos del cliente actualizados con éxito');
            this.cargarClientes();
            this.cerrarModal();
          },
          error: (err) => alert(typeof err.error === 'string' ? err.error : 'Error al actualizar cliente')
        });
      } else {
        // MODO CREACIÓN (POST)
        this.http.post('http://localhost:8080/api/admin/clientes', payload).subscribe({
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