import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Api, VerifyEmailRequest } from '../../services/api'; // Asegúrate de que Api exporte VerifyEmailRequest
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';

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
    MatButtonModule
  ],
  templateUrl: './verify-email.html',
  styleUrls: ['./verify-email.scss']
})
export class VerifyEmail implements OnInit {

  verifyForm;
  userEmail: string | null = null;
  serverError: string | null = null;
  serverSuccess: string | null = null;

  // --- LÓGICA DE LÍMITE DE INTENTOS! ---
  private attemptCount: number = 0;
  private maxAttempts: number = 5;
  public isBlocked: boolean = false;

  constructor(
    private fb: FormBuilder,
    private apiService: Api,
    private router: Router,
    private route: ActivatedRoute // ¡Para leer la URL!
  ) {
    this.verifyForm = this.fb.group({
      // Valida un código de 6 dígitos
      otpCode: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]]
    });
  }

  ngOnInit(): void {
    // 1. Lee el email de la URL (ej. /verify-email?email=test@test.com)
    this.route.queryParamMap.subscribe(params => {
      this.userEmail = params.get('email');
      if (!this.userEmail) {
        // Si no hay email, no tiene sentido estar aquí
        this.router.navigate(['/register']);
      }
    });

    // Borra el error si el usuario empieza a escribir
    this.verifyForm.valueChanges.subscribe(() => {
      this.serverError = null;
      this.serverSuccess = null;
    });
  }

onSubmit(): void {
    // 1. Obtenemos los valores PRIMERO
    const otpCodeValue = this.verifyForm.value.otpCode;

    if (this.verifyForm.invalid || !this.userEmail || !otpCodeValue || this.isBlocked) {
      return;
    }
    // 2. ¡EL "GUARDIÁN" MEJORADO!
    // Comprueba si el formulario es inválido O si alguno de los valores es nulo/undefined
    if (this.verifyForm.invalid || !this.userEmail || !otpCodeValue) {
      this.serverError = "Por favor, introduce un código de 6 dígitos.";
      return;
    }

    this.serverError = null;
    this.serverSuccess = null;

    // 3. ¡Ahora TypeScript está feliz!
    // Sabe que 'this.userEmail' y 'otpCodeValue' NO son nulos aquí.
    const request: VerifyEmailRequest = {
      email: this.userEmail,
      otpCode: otpCodeValue
    };

    // 4. Llama a la API (el resto del método es igual)
    this.apiService.verifyEmail(request).subscribe({
      next: () => {
        alert('¡Cuenta verificada! Ahora puedes iniciar sesión.');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        // --- ¡LÓGICA DE ERROR MEJORADA! ---
        if (err.status === 400) {
          
          this.serverError = err.error;
          
          // ¡Comprueba si fue un código incorrecto!
          if (err.error.includes('incorrecto')) {
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

    

    // 4. Llama a la API de reenvío
    this.apiService.resendVerificationCode(this.userEmail).subscribe({
      next: () => {
        // ¡ÉXITO!
        this.serverSuccess = '¡Se ha enviado un nuevo código a tu email!';

        // --- ¡RESETEA EL CONTADOR! ---
        this.serverError = null;
        this.attemptCount = 0;
        this.isBlocked = false;
      },
      error: (err) => {
        if (err.status === 400) {
          this.serverError = err.error;
        } else {
          this.serverError = 'Ocurrió un error inesperado.';
        }
      }
    });
  }
}