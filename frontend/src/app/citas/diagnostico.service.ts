import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Diagnostico } from './diagnostico.model';

@Injectable({
  providedIn: 'root'
})
export class DiagnosticoService {
  
  private apiUrl = 'http://localhost:8080/api/diagnosticos';

  constructor(private http: HttpClient) { }

  registrarDiagnostico(citaId: number, diagnostico: Diagnostico): Observable<Diagnostico> {
    return this.http.post<Diagnostico>(`${this.apiUrl}/cita/${citaId}`, diagnostico);
  }

  obtenerDiagnosticoPorCita(citaId: number): Observable<Diagnostico> {
    return this.http.get<Diagnostico>(`${this.apiUrl}/cita/${citaId}`);
  }
}