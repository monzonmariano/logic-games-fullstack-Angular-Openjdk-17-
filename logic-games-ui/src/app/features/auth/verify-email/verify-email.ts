import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Api, VerifyEmailRequest } from '../../../core/services/api';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon'; // <-- IMPORTADO

@Component({
  selector: 'app-verify-email',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule // <-- REGISTRADO
  ],
  templateUrl: './verify-email.html',
  styleUrls: ['./verify-email.scss']
})
export class VerifyEmail implements OnInit {

  verifyForm;
  userEmail: string | null = null;
  serverError: string | null = null;
  serverSuccess: string | null = null;

  private attemptCount: number = 0;
  private maxAttempts: number = 5;
  public isBlocked: boolean = false;

  constructor(
    private fb: FormBuilder,
    private apiService: Api,
    private router: Router,
    private route: ActivatedRoute 
  ) {
    this.verifyForm = this.fb.group({
      otpCode: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.userEmail = params.get('email');
      if (!this.userEmail) {
        this.router.navigate(['/register']);
      }
    });

    this.verifyForm.valueChanges.subscribe(() => {
      this.serverError = null;
      this.serverSuccess = null;
    });
  }

  onSubmit(): void {
    const otpCodeValue = this.verifyForm.value.otpCode;

    if (this.verifyForm.invalid || !this.userEmail || !otpCodeValue || this.isBlocked) {
      return;
    }
    if (this.verifyForm.invalid || !this.userEmail || !otpCodeValue) {
      this.serverError = "Por favor, introduce un código de 6 dígitos.";
      return;
    }

    this.serverError = null;
    this.serverSuccess = null;

    const request: VerifyEmailRequest = {
      email: this.userEmail,
      otpCode: otpCodeValue
    };

    this.apiService.verifyEmail(request).subscribe({
      next: () => {
        alert('¡Cuenta verificada! Ahora puedes iniciar sesión.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        if (err.status === 400) {
          // FIX: Prevención de null object
          let errorMessage = err.error;
          if (err.error !== null && typeof err.error === 'object') {
             errorMessage = err.error.message || 'Código inválido.';
          }
          
          this.serverError = errorMessage;
          
          if (typeof errorMessage === 'string' && errorMessage.includes('incorrecto')) {
            this.attemptCount++;
            if (this.attemptCount >= this.maxAttempts) {
              this.isBlocked = true;
              this.serverError = "Demasiados intentos fallidos. Por favor, reenvía un nuevo código.";
            } else {
              this.serverError += ` (Intento ${this.attemptCount} de ${this.maxAttempts})`;
            }
          }
        } else {
          this.serverError = 'Ocurrió un error inesperado.';
        }
      }
    });
  }

  onResendCode(): void {
    if (!this.userEmail) return;

    this.apiService.resendVerificationCode(this.userEmail).subscribe({
      next: () => {
        this.serverSuccess = '¡Se ha enviado un nuevo código a tu email!';
        this.serverError = null;
        this.attemptCount = 0;
        this.isBlocked = false;
      },
      error: (err) => {
        if (err.status === 400) {
          // FIX: Prevención de null
          if (err.error !== null && typeof err.error === 'object') {
            this.serverError = err.error.message;
          } else {
            this.serverError = err.error;
          }
        } else {
          this.serverError = 'Ocurrió un error inesperado.';
        }
      }
    });
  }
}