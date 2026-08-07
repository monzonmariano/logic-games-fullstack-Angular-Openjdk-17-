import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule ,FormBuilder, Validators, AbstractControl } from '@angular/forms';
import { Router } from '@angular/router';
import { Api } from '../../../core/services/api';

import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon'; // <-- IMPORTADO

function passwordMatchValidator(control: AbstractControl) {
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;
  if (password !== confirmPassword) {
    return { passwordMismatch: true };
  } else {
    return null;
  }
}

@Component({
  selector: 'app-register',
  imports: [
    CommonModule, 
    ReactiveFormsModule, 
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCardModule,
    MatIconModule // <-- REGISTRADO
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register implements OnInit {
   
    registerForm;
    public serverError: string | null = null;
    
    // <-- ESTADOS DE LOS OJITOS
    public hidePassword = true;
    public hideConfirmPassword = true;
    
    private emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    private passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@!%*?&]).{8,}$/;

    constructor(
      private fb: FormBuilder,
      private apiService: Api,
      private router: Router
    ) {
     this.registerForm = this.fb.group({
         email:['',[Validators.required,Validators.pattern(this.emailPattern)]],
         password:['',[
          Validators.required,
          Validators.minLength(8),
          Validators.pattern(this.passwordPattern)         
        ]],
         confirmPassword:['',Validators.required]
     },{
         validators: [passwordMatchValidator]
     });
    }

    ngOnInit(): void {
      this.registerForm.valueChanges.subscribe(() => {
        this.serverError = null;
      });
    }

    onSubmit() {
  if (this.registerForm.invalid) {
    return;
  }
  
  this.serverError = null; 

  this.apiService.register(this.registerForm.value)
    .subscribe({ 
      next: () => {
        const email = this.registerForm.value.email;
        this.router.navigate(['/verify-email'], { 
          queryParams: { email: email } 
        });
      },
      error: (err) => {
        console.error('Error en el registro:', err);
        
        if (err.status === 400) { 
          // FIX: Verificamos que no sea null y vemos si es un objeto JSON
          if (err.error !== null && typeof err.error === 'object') {
            this.serverError = err.error.message || 'Datos inválidos. Verifica tu información.';
          } else {
            // Si es un texto simple (ej. "El email ya está en uso")
            this.serverError = err.error; 
          }
        } else {
          this.serverError = 'Ocurrió un error inesperado. Por favor, intente más tarde.';
        }
      }
    });
}
}