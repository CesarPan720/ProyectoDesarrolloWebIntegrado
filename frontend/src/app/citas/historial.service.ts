import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { HistorialMedico } from './historial.model';

@Injectable({
  providedIn: 'root'
})
export class HistorialService {
  private apiUrl = 'http://localhost:8080/api/historiales';

  constructor(private http: HttpClient) { }

  registrarAtencion(citaId: number, datos: HistorialMedico): Observable<HistorialMedico> {
    return this.http.post<HistorialMedico>(`${this.apiUrl}/cita/${citaId}`, datos);
  }

  obtenerHistorialPorMascota(mascotaId: number): Observable<HistorialMedico[]> {
    return this.http.get<HistorialMedico[]>(`${this.apiUrl}/mascota/${mascotaId}`);
  }
}