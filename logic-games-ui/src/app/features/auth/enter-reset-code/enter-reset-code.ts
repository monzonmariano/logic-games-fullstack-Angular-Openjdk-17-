import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { FormBuilder, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { Api } from '../../../core/services/api';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon'; // <-- IMPORTADO

function passwordMatchValidator(control: AbstractControl) {
  const password = control.get('newPassword')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordMismatch: true };
}

@Component({
  selector: 'app-enter-reset-code',
  standalone: true,
  imports: [
    CommonModule, ReactiveFormsModule, RouterLink,
    MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule,
    MatIconModule // <-- REGISTRADO
  ],
  templateUrl: './enter-reset-code.html',
  styleUrls: ['./enter-reset-code.scss']
})
export class EnterResetCode implements OnInit {

  resetForm;
  userEmail: string | null = null;
  serverError: string | null = null;
  serverSuccess: string | null = null;
  private passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@!%*?&]).{8,}$/;
  
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
      otpCode: ['', [Validators.required, Validators.pattern(/^\d{6}$/)]],
      newPassword: ['', [Validators.required, Validators.pattern(this.passwordPattern)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: [passwordMatchValidator] });
  }

  ngOnInit(): void {
    this.route.queryParamMap.subscribe(params => {
      this.userEmail = params.get('email');
      if (!this.userEmail) {
        this.router.navigate(['/forgot-password']);
      }
    });
  }

  onSubmit() {
    if (this.resetForm.invalid || !this.userEmail) return;

    this.serverError = null;
    
    this.apiService.resetPasswordWithCode({
      email: this.userEmail,
      otpCode: this.resetForm.value.otpCode!,
      newPassword: this.resetForm.value.newPassword!
    }).subscribe({
      next: () => {
        alert('¡Contraseña actualizada con éxito!');
        this.router.navigate(['/login']);
      },
      error: (err) => {
        // FIX: Prevención del error null de JavaScript
        if (err.error !== null && typeof err.error === 'object') {
          this.serverError = err.error.message || 'Error al resetear contraseña.';
        } else {
          this.serverError = err.error || 'Error al resetear contraseña.';
        }
      }
    });
  }
}