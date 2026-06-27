import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../auth';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './register.html',
  styleUrls: ['./register.css']
})
export class RegisterComponent implements OnInit {
  registroForm!: FormGroup;
  mensajeError: string = '';
  mensajeExito: string = '';
  mostrarPassword = false;
  mostrarConfirmarPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.registroForm = this.fb.group({
      nombreCompleto: ['', Validators.required],
      email: ['', [Validators.required, Validators.email]],
      telefono: ['', Validators.required],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmarPassword: ['', Validators.required]
    }, { validators: this.passwordsMatchValidator });
  }

  passwordsMatchValidator(form: FormGroup) {
    const password = form.get('password')?.value;
    const confirm = form.get('confirmarPassword')?.value;
    return password === confirm ? null : { mismatch: true };
  }

  registrar() {
    if (this.registroForm.valid) {
      this.mensajeError = '';
      this.mensajeExito = '';

      const payload = {
        ...this.registroForm.value,
        tipoUsuario: 'CLIENTE'
      };

      this.authService.registrar(payload).subscribe({
        next: (res) => {
          this.mensajeExito = '¡Usuario creado exitosamente!';
          this.registroForm.reset();

          this.cdr.detectChanges();

          setTimeout(() => {
            this.mensajeExito = '';
            this.cdr.detectChanges();
          }, 3500);
        },
        error: (err) => {
          console.error('Log completo del error:', err);

          if (err.error && err.error.error) {
            this.mensajeError = err.error.error;
          } else if (typeof err.error === 'string') {
            this.mensajeError = err.error;
          } else {
            this.mensajeError = 'Error de validación: Verifica los datos o intenta con otro correo.';
          }

          this.cdr.detectChanges();

          setTimeout(() => {
            this.mensajeError = '';
            this.cdr.detectChanges();
          }, 3500);
        }
      });
    }
  }
}
