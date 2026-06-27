import { Routes } from '@angular/router';
import { RegisterComponent } from './auth/register/register';
import { LoginComponent } from './auth/login/login';
import { DashboardComponent } from './dashboard/dashboard';
import { MascotasComponent } from './mascotas/mascotas'
import { UsuariosComponent } from './admin/usuarios/usuarios';
import { VeterinariosComponent } from './admin/veterinarios/veterinarios';
import { ClientesComponent } from './admin/clientes/clientes';
import { CitasComponent } from './citas/citas';
import { RoleGuard } from './auth/guards/role-guard';

export const routes: Routes = [
// RUTAS PÚBLICAS (Sin protección)
{ path: 'registro', component: RegisterComponent },
{ path: 'login', component: LoginComponent },

// RUTAS COMPARTIDAS (Todos los logueados)
{
path: 'dashboard',
component: DashboardComponent,
canActivate: [RoleGuard],
data: { roles: ['ADMIN', 'CLIENTE', 'VETERINARIO'] }
},
{
path: 'mascotas',
component: MascotasComponent,
canActivate: [RoleGuard],
data: { roles: ['ADMIN', 'CLIENTE', 'VETERINARIO'] }
},
{
path: 'citas',
component: CitasComponent,
canActivate: [RoleGuard],
data: { roles: ['ADMIN', 'CLIENTE', 'VETERINARIO'] }
},

// RUTAS EXCLUSIVAS DEL ADMINISTRADOR
{
path: 'admin/veterinarios',
component: VeterinariosComponent,
canActivate: [RoleGuard],
data: { roles: ['ADMIN'] }
},
{
path: 'admin/clientes',
component: ClientesComponent,
canActivate: [RoleGuard],
data: { roles: ['ADMIN'] }
},
{
path: 'admin/usuarios',
component: UsuariosComponent,
canActivate: [RoleGuard],
data: { roles: ['ADMIN'] }
},

// REDIRECCIONES POR DEFECTO
{ path: '', redirectTo: '/login', pathMatch: 'full' },
{ path: '**', redirectTo: '/login' }
];
