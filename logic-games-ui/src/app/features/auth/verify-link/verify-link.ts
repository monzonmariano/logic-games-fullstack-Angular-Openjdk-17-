import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Api } from '../../../core/services/api';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon'; // <-- IMPORTADO

@Component({
  selector: 'app-verify-link',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatProgressSpinnerModule,
    MatIconModule // <-- REGISTRADO
  ],
  templateUrl: './verify-link.html',
  styleUrls: ['./verify-link.scss']
})
export class VerifyLink implements OnInit{

  public isLoading: boolean = true;
  public statusMessage: string = "Verificando tu cuenta...";
  public isError: boolean = false;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private apiService: Api
  ) {}

  ngOnInit(): void {
    const token = this.route.snapshot.queryParamMap.get('token');

    if (!token) {
      this.isLoading = false;
      this.isError = true;
      this.statusMessage = "Enlace inválido. No se encontró ningún token.";
      return;
    }

    this.apiService.verifyEmailLink(token).subscribe({
      next: (response) => {
        this.isLoading = false;
        this.isError = false;
        this.statusMessage = response + " ¡Serás redirigido al login en 3 segundos!";
        
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 3000);
      },
      error: (err) => {
        this.isLoading = false;
        this.isError = true;
        
        // FIX: Prevención del error null de JavaScript
        if (err.error !== null && typeof err.error === 'object') {
          this.statusMessage = err.error.message || "No se pudo verificar tu cuenta.";
        } else {
          this.statusMessage = err.error || "No se pudo verificar tu cuenta.";
        }
      }
    });
  }
}