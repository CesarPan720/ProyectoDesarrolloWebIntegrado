export interface HistorialMedico {
  id?: number;
  fechaAtencion?: string;
  temperatura: string;
  sintomas: string;
  diagnostico: string;
  tratamiento: string;
  mascota?: any;
  veterinario?: any;
}