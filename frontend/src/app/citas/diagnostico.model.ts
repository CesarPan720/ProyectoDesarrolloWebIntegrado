export interface Diagnostico {
  id?: number;
  descripcion: string;
  recetaMedica: string;
  // No necesitamos mandar la cita completa en el objeto, 
  // ya que asociaremos el ID directamente a través de la URL del endpoint.
}