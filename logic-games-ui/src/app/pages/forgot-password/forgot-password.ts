import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';

import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink,Router } from '@angular/router';
import { Api } from '../../services/api';

import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-forgot-password',
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    RouterLink,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCardModule
  ],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.scss',
})
export class ForgotPassword {

  forgotForm;
  private emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;

  // ¡Variable para darle feedback al usuario!
  public message: string = "";

  constructor(
    private fb: FormBuilder,
    private apiService: Api,
    private router: Router
  ) {
    this.forgotForm = this.fb.group({
      email: ['', [Validators.required, Validators.pattern(this.emailPattern)]]
    });
  }
  onSubmit() {
    if (this.forgotForm.invalid) return;

    const email = this.forgotForm.value.email as string;
    this.message = "Procesando...";

    this.apiService.requestPasswordReset(email).subscribe({
      next: (response) => {
        // ¡ÉXITO!
        // ¡En lugar de mostrar un mensaje, REDIRIGIMOS!
        this.router.navigate(['/enter-reset-code'], {
          queryParams: { email: email }
        });
      },
      error: (err) => {
        console.error(err);
        this.message = err.error || "Error en el servidor. Inténtalo más tarde.";
      }
    });
  }
}
