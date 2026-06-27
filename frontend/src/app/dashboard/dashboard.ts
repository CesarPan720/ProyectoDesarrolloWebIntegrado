import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule, Router } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth';
import { SidebarComponent } from '../shared/sidebar/sidebar';

@Component({
selector: 'app-dashboard',
standalone: true,
imports: [CommonModule, RouterModule, SidebarComponent],
templateUrl: './dashboard.html',
styleUrls: ['./dashboard.css']
})
export class DashboardComponent implements OnInit {
userRole: string | null = '';
nombreUsuario: string = 'Usuario';

// Datos para ADMIN
stats = {
totalUsuarios: 0,
totalPacientes: 0,
totalVeterinarios: 0,
citasProgramadas: 0
};
citasDelDiaAdmin: any[] = [];

// Datos para CLIENTE
misMascotas: any[] = [];
proximaCita: any = null;
citasHoyCliente: any[] = []; // NUEVA VARIABLE AÑADIDA

//Datos para VETERINARIO
pacientesVeterinario: any[] = [];
citasHoyVeterinario: any[] = [];

constructor(
    private authService: AuthService,
    private router: Router,
    private http: HttpClient,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.userRole = this.authService.obtenerRol();

    this.http.get<any>('http://localhost:8080/api/perfil/nombre').subscribe({
      next: (res) => {
        this.nombreUsuario = res.nombre;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al obtener el nombre de perfil:', err)
    });

    if (this.userRole === 'ADMIN') {
      this.cargarEstadisticasAdmin();
    } else if (this.userRole === 'CLIENTE') {
      this.cargarDatosCliente();
      this.cargarCitasCliente();
    } else if (this.userRole === 'VETERINARIO') {
      this.cargarDatosVeterinario();
    }
  }

  // LÓGICA DEL ADMINISTRADOR
  cargarEstadisticasAdmin() {
    this.http.get<any>('http://localhost:8080/api/admin/dashboard/stats').subscribe({
      next: (data) => {
        this.stats = {
          totalUsuarios: data.totalUsuarios || 0,
          totalVeterinarios: data.totalVeterinarios || 0,
          totalPacientes: data.totalPacientes || 0,
          citasProgramadas: data.citasProgramadas || 0
        };
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar stats admin:', err)
    });
  }

  // LÓGICA DEL CLIENTE
  cargarDatosCliente() {
    this.http.get<any[]>('http://localhost:8080/api/mascotas').subscribe({
      next: (data) => {
        this.misMascotas = data;
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar mascotas del cliente:', err)
    });
  }

  cargarDatosVeterinario() {
    this.http.get<any[]>('http://localhost:8080/api/citas').subscribe({
      next: (data) => {
        // 1. EXTRAER PACIENTES ÚNICOS
        const mascotasMap = new Map();
        data.forEach(cita => {
          if (cita.mascota && !mascotasMap.has(cita.mascota.id)) {
            mascotasMap.set(cita.mascota.id, cita.mascota);
          }
        });
        this.pacientesVeterinario = Array.from(mascotasMap.values());

        // 2. FILTRAR LA AGENDA DE HOY
        const ahora = new Date();
        const tiempoActual = ahora.getTime(); // Reemplaza inicioDia (00:00) por los milisegundos de la hora exacta actual
        const finDia = new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate()).getTime() + 24 * 60 * 60 * 1000;

        this.citasHoyVeterinario = data.filter(c => {
          const fechaCita = new Date(c.fechaHora).getTime();
          return c.estado === 'PENDIENTE' && fechaCita >= tiempoActual && fechaCita < finDia;
        });

        this.citasHoyVeterinario.sort((a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime());

        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar datos del vet', err)
    });
  }

  cargarCitasCliente() {
    this.http.get<any[]>('http://localhost:8080/api/citas').subscribe({
      next: (data) => {
        const ahora = new Date();
        const tiempoActual = ahora.getTime();
        const finDia = new Date(ahora.getFullYear(), ahora.getMonth(), ahora.getDate()).getTime() + 24 * 60 * 60 * 1000;

        // 1. Filtramos solo las citas PENDIENTES que sean de hoy en adelante
        const citasPendientes = data.filter(c =>
          c.estado === 'PENDIENTE' && new Date(c.fechaHora).getTime() >= tiempoActual
        );

        citasPendientes.sort((a, b) =>
          new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime()
        );

        if (citasPendientes.length > 0) {
          this.proximaCita = citasPendientes[0];
        } else {
          this.proximaCita = null;
        }

        // 2. Extraemos la agenda de consultas específicas de HOY para el cliente
        this.citasHoyCliente = data.filter(c => {
          const fechaCita = new Date(c.fechaHora).getTime();
          return c.estado === 'PENDIENTE' && fechaCita >= tiempoActual && fechaCita < finDia;
        });

        this.citasHoyCliente.sort((a, b) => new Date(a.fechaHora).getTime() - new Date(b.fechaHora).getTime());
        this.cdr.detectChanges();
      },
      error: (err) => console.error('Error al cargar las citas para el dashboard', err)
    });
  }

  cerrarSesion() {
    this.authService.cerrarSesion();
    this.router.navigate(['/login']);
  }
}
