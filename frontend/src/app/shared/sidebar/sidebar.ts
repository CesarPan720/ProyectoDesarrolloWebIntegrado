import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AuthService } from '../../auth/auth';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './sidebar.html',
  styleUrls: ['./sidebar.css']
})
export class SidebarComponent implements OnInit {
userRole: string | null = '';

isSidebarOpen = false;

constructor(private authService: AuthService) {}

  ngOnInit(): void {
    this.userRole = this.authService.obtenerRol();
  }

  // NUEVO: Abre o cierra el menú al tocar el botón hamburguesa
  toggleSidebar() {
    this.isSidebarOpen = !this.isSidebarOpen;
  }

  // NUEVO: Cierra el menú al hacer clic en un enlace (ideal para celular)
  closeSidebar() {
    this.isSidebarOpen = false;
  }
}
