import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { SidebarComponent } from '../../shared/sidebar/sidebar';

@Component({
selector: 'app-usuarios',
standalone: true,
imports: [CommonModule, ReactiveFormsModule, SidebarComponent],
templateUrl: './usuarios.html',
styleUrls: ['./usuarios.css']
})
export class UsuariosComponent implements OnInit {
usuarios: any[] = [];
mostrarModal = false;
usuarioForm!: FormGroup;
idUsuarioEditar: number | null = null;

constructor(
    private http: HttpClient,
    private fb: FormBuilder,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarUsuarios();

    this.usuarioForm = this.fb.group({
      nombreCompleto: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]], // Agregamos el email
      telefono: ['', Validators.required],
      rol: ['CLIENTE', Validators.required],
      password: ['']
    });
  }

  cargarUsuarios() {
    this.http.get<any[]>('http://localhost:8080/api/admin/usuarios').subscribe({
      next: (data) => {
        this.usuarios = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar usuarios', err)
    });
  }

  cambiarEstado(id: number, estadoActual: boolean) {
    const accion = estadoActual ? 'dar de baja' : 'reactivar';
    if (confirm(`¿Estás seguro de que deseas ${accion} a este usuario?`)) {
      this.http.put(`http://localhost:8080/api/admin/usuarios/${id}/estado`, {}).subscribe({
        next: () => {
          alert(`Usuario ${estadoActual ? 'dado de baja' : 'reactivado'} correctamente`);
          this.cargarUsuarios();
        },
        error: (err) => console.error('Error al cambiar estado', err)
      });
    }
  }

  // NUEVO: Método para abrir el modal vacío para crear
  abrirModalNuevo() {
    this.idUsuarioEditar = null;
    this.usuarioForm.reset({ rol: 'CLIENTE' });

    // Habilitamos el email y hacemos la contraseña obligatoria
    this.usuarioForm.get('email')?.enable();
    this.usuarioForm.get('password')?.setValidators([Validators.required, Validators.minLength(6)]);
    this.usuarioForm.get('password')?.updateValueAndValidity();

    this.mostrarModal = true;
  }

  // ACTUALIZADO: Método para editar
  abrirModalEdicion(usuario: any) {
    this.idUsuarioEditar = usuario.id;
    this.usuarioForm.patchValue({
      nombreCompleto: usuario.nombreCompleto,
      email: usuario.email,
      telefono: usuario.telefono,
      rol: usuario.rol,
      password: ''
    });

    // CAMBIO AQUÍ: Mantenemos el campo de correo habilitado para edición
    this.usuarioForm.get('email')?.enable();

    this.usuarioForm.get('password')?.clearValidators();
    this.usuarioForm.get('password')?.updateValueAndValidity();

    this.mostrarModal = true;
  }

  cerrarModal() {
    this.mostrarModal = false;
    this.idUsuarioEditar = null;
    this.usuarioForm.reset();
  }

  guardarCambios() {
    if (this.usuarioForm.valid) {
      if (this.idUsuarioEditar) {
        // MODO EDICIÓN (PUT)
        this.http.put(`http://localhost:8080/api/admin/usuarios/${this.idUsuarioEditar}`, this.usuarioForm.getRawValue()).subscribe({
          next: () => {
            alert('Usuario actualizado con éxito');
            this.cargarUsuarios();
            this.cerrarModal();
          },
          error: (err) => alert('Error al actualizar')
        });
      } else {
        // MODO CREACIÓN (POST) -> Ahora apunta al nuevo endpoint del Admin
        const payload = {
          nombreCompleto: this.usuarioForm.value.nombreCompleto,
          email: this.usuarioForm.value.email,
          telefono: this.usuarioForm.value.telefono,
          password: this.usuarioForm.value.password,
          rol: this.usuarioForm.value.rol // Usamos 'rol' directamente
        };

        this.http.post('http://localhost:8080/api/admin/usuarios', payload).subscribe({
          next: () => {
            alert('Usuario registrado con éxito');
            this.cargarUsuarios();
            this.cerrarModal();
          },
          error: (err) => {
            console.error(err);
            alert('Error al crear usuario. Verifica que el correo no esté repetido.');
          }
        });
      }
    }
  }
}
