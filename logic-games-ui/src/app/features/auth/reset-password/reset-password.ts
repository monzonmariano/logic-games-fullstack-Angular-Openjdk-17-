import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Api } from '../../../core/services/api';
import { MatCard, MatCardActions, MatCardTitle } from "@angular/material/card";
import { MatError } from "@angular/material/input";
import { MatAnchor } from "@angular/material/button";
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon'; // <-- IMPORTADO

function passwordMatchValidator(control: AbstractControl) {
  const password = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (password !== confirmPassword) {
    return { passwordMismatch: true };
  }
  return null;
}

@Component({
  selector: 'app-reset-password',
  imports: [CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCard,
    MatError,
    MatCardActions,
    MatAnchor,
    MatCardTitle,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule // <-- REGISTRADO
  ],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.scss',
})

export class ResetPassword implements OnInit {

  resetForm;
  private passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@!%*?&]).{8,}$/;

  private token: string | null = null;
  public message: string = "";
  public isError: boolean = false;
  
  // <-- VARIABLES DEL OJITO
  public hidePassword = true;
  public hideConfirmPassword = true;

  constructor(
    private fb: FormBuilder,
    private apiService: Api,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.pattern(this.passwordPattern)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: [passwordMatchValidator]
    });
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.token = params.get('token');
      if (!this.token) {
        this.isError = true;
        this.message = "Error: No se ha proporcionado un token de reseteo.";
      }
    });
  }

  onSubmit() {
    if (this.resetForm.invalid || !this.token) {
      return;
    }

    this.message = "Actualizando...";
    this.isError = false;

    const newPassword = this.resetForm.value.newPassword as string;

    this.apiService.resetPassword({ token: this.token, newPassword: newPassword })
      .subscribe({
        next: () => {
          this.message = "¡Contraseña actualizada! Serás redirigido al login.";
          this.isError = false;
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (err) => {
          this.isError = true;
          // FIX: Prevención del error null de JavaScript
          if (err.error !== null && typeof err.error === 'object') {
            this.message = err.error.message || "Error: El token es inválido o ha caducado.";
          } else {
            this.message = err.error || "Error: El token es inválido o ha caducado.";
          }
        }
      });
  }
}