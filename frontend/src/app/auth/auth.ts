import { Injectable } from '@angular/core';
import { HttpClient, HttpInterceptorFn } from '@angular/common/http'; // <-- Agregamos HttpInterceptorFn aquí
import { tap } from 'rxjs/operators';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private apiUrl = 'http://localhost:8080/api/auth';

  constructor(private http: HttpClient) {}

  login(credenciales: any) {
    return this.http.post<any>(`${this.apiUrl}/login`, credenciales).pipe(
      tap(response => {
        if (response.token) {
          localStorage.setItem('token', response.token);
        }
      })
    );
  }

  registrar(usuario: any) {
    return this.http.post<any>(`${this.apiUrl}/registro`, usuario);
  }

  obtenerRol(): string | null {
    const token = localStorage.getItem('token');
    if (!token) return null;
    try {
      const payload = token.split('.')[1];
      const decoded = JSON.parse(atob(payload));
      return decoded.rol;
    } catch (e) {
      return null;
    }
  }

  cerrarSesion() {
    localStorage.removeItem('token');
  }
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('token');
  if (token) {
    const reqClonada = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(reqClonada);
  }
  return next(req);
};
