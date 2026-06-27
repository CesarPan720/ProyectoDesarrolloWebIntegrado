import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './login.html',
  styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit {
  loginForm!: FormGroup;
  mensajeError: string = '';
  mostrarPassword = false;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', Validators.required]
    });
  }

  iniciarSesion() {
    if (this.loginForm.valid) {
      this.mensajeError = '';

      this.authService.login(this.loginForm.value).subscribe({
        next: (res) => {
          const rol = this.authService.obtenerRol();
          console.log('Login exitoso. Rol:', rol);

          this.router.navigate(['/dashboard']);
        },
        error: (err) => {
          console.error('Fallo el login', err);

          if (err.error && err.error.error) {
            this.mensajeError = err.error.error;
          } else if (typeof err.error === 'string') {
            this.mensajeError = err.error;
          } else {
            this.mensajeError = 'Credenciales incorrectas. Verifica tu correo y contraseña.';
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
