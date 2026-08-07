import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { Api } from '../../../core/services/api';

import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon'; // <-- IMPORTADO

@Component({
  selector: 'app-forgot-password',
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
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss',
})
export class ForgotPassword {

  forgotForm;
  private emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

  public message: string = "";
  public isError: boolean = false; // <-- ESTADO PARA EL BANNER

  constructor(
    private fb: FormBuilder,
    private apiService: Api,
    private router: Router
  ) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.pattern(this.emailPattern)]]
    });

    // Limpiamos el mensaje si el usuario corrige su email
    this.forgotForm.valueChanges.subscribe(() => {
      this.message = "";
      this.isError = false;
    });
  }

  onSubmit() {
    if (this.forgotForm.invalid) return;

    const email = this.forgotForm.value.email as string;
    this.message = "Procesando...";
    this.isError = false; // "Procesando" usa el banner verde/info

    this.apiService.requestPasswordReset(email).subscribe({
      next: (response) => {
        // Redirigimos a la pantalla de ingresar el código
        this.router.navigate(['/enter-reset-code'], {
          queryParams: { email: email }
        });
      },
      error: (err) => {
        console.error('Error en forgot-password:', err);
        this.isError = true;
        
        // FIX: Prevención del error null de JavaScript
        if (err.error !== null && typeof err.error === 'object') {
          this.message = err.error.message || "Error en el servidor. Inténtalo más tarde.";
        } else {
          this.message = err.error || "Error en el servidor. Inténtalo más tarde.";
        }
      }
    });
  }
}