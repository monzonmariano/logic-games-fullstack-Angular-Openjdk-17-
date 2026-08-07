import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../../../core/services/api';
import { AuthService } from '../../../core/services/auth';

import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon'; // <-- IMPORTADO

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule // <-- REGISTRADO
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})
export class Login implements OnInit {
  loginForm;
  public serverError: string | null = null;
  public isNotVerifiedError: boolean = false;
  
  public hidePassword = true; // <-- ESTADO DEL OJITO

  private emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

  constructor(
    private fb: FormBuilder,
    private apiService: Api,
    private router: Router,
    private authService: AuthService
  ) {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.pattern(this.emailPattern)]],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    this.loginForm.valueChanges.subscribe(() => {
      this.serverError = null;
      this.isNotVerifiedError = false;
    });
  }

  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }
    
    this.serverError = null;
    this.isNotVerifiedError = false;

    this.apiService.login(this.loginForm.value)
      .subscribe({
        next: (response) => {
          this.authService.saveToken(response.token);
          this.router.navigate(['/']);
        },
        error: (err) => {
          console.error('Error en el login:', err);
          
          if (err.status === 0 || err.status === 504) {
            this.serverError = "El servidor está despertando. Por favor, intenta de nuevo en unos segundos.";
            return;
          }

          let errorMessage = err.error || err.message;
          
          // FIX: Añadimos err.error !== null para evitar el crash de JavaScript
          if (err.error !== null && typeof err.error === 'object') {
            errorMessage = err.error.message || JSON.stringify(err.error);
          }

          // FIX: Aseguramos que errorMessage exista antes de usar .includes()
          if (err.status === 400 && errorMessage && errorMessage.includes('verifica tu email')) {
            alert("Tu cuenta no está verificada. Te redirigimos a la página de verificación.");
            const email = this.loginForm.get('email')?.value;
            this.router.navigate(['/verify-email'], {
              queryParams: { email: email } 
            });
          } else if (err.status === 401 || err.status === 403) {
            this.serverError = "Usuario o Contraseña inválido";
          } else {
            this.serverError = errorMessage;
          }
        }
      });
  }

  onResendCode(): void {
    const email = this.loginForm.get('email')?.value;
    if (!email) {
      this.serverError = "Por favor, escribe tu email en el campo de arriba para reenviar.";
      return;
    }
    this.apiService.resendVerificationCode(email).subscribe({
      next: () => {
        this.isNotVerifiedError = false;
        this.serverError = "¡Código reenviado! Revisa tu email.";
      },
      error: (err) => {
        this.serverError = err.error || "No se pudo reenviar el código.";
      }
    });
  }

  goToVerification(): void {
    const email = this.loginForm.get('email')?.value;
    if (email) {
      this.router.navigate(['/verify-email'], { queryParams: { email: email } });
    } else {
      this.router.navigate(['/verify-email']);
    }
  }
}