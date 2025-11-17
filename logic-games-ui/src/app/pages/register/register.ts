import { Component, OnInit } from '@angular/core'; // <-- Añade OnInit
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule ,FormBuilder,Validators,AbstractControl} from '@angular/forms';
import { Router } from '@angular/router';
import { Api } from '../../services/api';

import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

// Tu validador de contraseña (¡perfecto!)
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
    MatCardModule
  ],
  templateUrl: './register.html',
  styleUrl: './register.scss',
})
export class Register implements OnInit { // <-- Implementa OnInit
   
    registerForm;
    
    // --- ¡NUEVA VARIABLE PARA ERRORES! ---
    public serverError: string | null = null;
    
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
      // ¡Buena UX! Si el usuario empieza a escribir de nuevo,
      // borra el error del servidor.
      this.registerForm.valueChanges.subscribe(() => {
        this.serverError = null;
      });
    }

    onSubmit() {
      if (this.registerForm.invalid) {
        return;
      }
      
      // Limpia errores viejos antes de enviar
      this.serverError = null; 

      this.apiService.register(this.registerForm.value)
        .subscribe({ 
          
          // --- ¡CAMBIO DE FASE 2! (Éxito) ---
          next: () => {
            // ¡Éxito! El backend envió el email.
            // Ahora, redirige a la página de verificación,
            // pasando el email en la URL.
            const email = this.registerForm.value.email;
            this.router.navigate(['/verify-email'], { 
              queryParams: { email: email } 
            });
          },
          
          // --- ¡CAMBIO DE FASE 1! (Error) ---
          error: (err) => {
            console.error('Error en el registro:', err);
            
            // El backend (AuthController) nos da el mensaje de error.
            // (ej. "El email ya está en uso")
            if (err.status === 400) { // 400 = Bad Request
              this.serverError = err.error; // err.error contiene el string de texto
            } else {
              this.serverError = 'Ocurrió un error inesperado. Por favor, intente más tarde.';
            }
          }
        });
    }
}