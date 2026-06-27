import { Injectable } from '@angular/core';
import { CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot, Router } from '@angular/router';
import { AuthService } from '../auth'; // Ajusta esta ruta a tu servicio de auth real

@Injectable({
providedIn: 'root'
})
export class RoleGuard implements CanActivate {

constructor(private authService: AuthService, private router: Router) {}

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const userRole = this.authService.obtenerRol();

    if (!userRole) {
      this.router.navigate(['/login']);
      return false;
    }

    const rolesPermitidos = route.data['roles'] as Array<string>;

    if (rolesPermitidos && !rolesPermitidos.includes(userRole)) {
      // Si intentó entrar a un lugar prohibido, lo mandamos al inicio
      this.router.navigate(['/dashboard']);
      return false;
    }

    return true;
  }
}
