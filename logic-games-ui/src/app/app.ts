import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet , RouterLink} from '@angular/router';

import { AuthService } from './services/auth';
import { Router } from '@angular/router';

import { MatToolbarModule } from '@angular/material/toolbar';
import { MatButtonModule } from '@angular/material/button';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule, 
    RouterOutlet,
    RouterLink,
    MatToolbarModule,
    MatButtonModule,      
  ], 
  templateUrl: './app.html',
  styleUrl: './app.scss'
})
export class App  { 
  
  // 5. Inyectamos el AuthService y el Router
  constructor(
    public authService: AuthService, // <-- ¡Lo hacemos PÚBLICO para usarlo en el HTML!
    private router: Router
  ) {}

  // 6. Creamos una función de logout
  logout() {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
  
}