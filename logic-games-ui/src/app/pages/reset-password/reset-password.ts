import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { Api } from '../../services/api';
import { MatCard, MatCardActions, MatCardTitle } from "@angular/material/card";
import { MatError } from "@angular/material/input";
import { MatAnchor } from "@angular/material/button";
import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

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
    MatCardModule],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.scss',
})

export class ResetPassword implements OnInit {

  resetForm;
  private passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@!%*?&]).{8,}$/;

  // ¡Variables para el estado!
  private token: string | null = null;
  public message: string = "";
  public isError: boolean = false;

  constructor(
    private fb: FormBuilder,
    private apiService: Api,
    private router: Router,
    private route: ActivatedRoute // <-- ¡El "Lector de URLs"!
  ) {
    this.resetForm = this.fb.group({
      newPassword: ['', [Validators.required, Validators.pattern(this.passwordPattern)]],
      confirmPassword: ['', [Validators.required]]
    }, {
      validators: [passwordMatchValidator] // ¡Validación cruzada!
    });
  }

  ngOnInit(): void {
    // ¡¡AQUÍ LEEMOS EL TOKEN!!
    // 'route.queryParamMap' lee los parámetros ?token=... de la URL
    this.route.queryParamMap.subscribe(params => {
      this.token = params.get('token'); // Coge el token de la URL
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

    // ¡Llamamos a la API!
    this.apiService.resetPassword({ token: this.token, newPassword: newPassword })
      .subscribe({
        next: () => {
          this.message = "¡Contraseña actualizada! Serás redirigido al login.";
          this.isError = false;
          // Espera 3 segundos y redirige
          setTimeout(() => {
            this.router.navigate(['/login']);
          }, 3000);
        },
        error: (err) => {
          this.isError = true;
          this.message = err.error || "Error: El token es inválido o ha caducado.";
        }
      });
  }

}
