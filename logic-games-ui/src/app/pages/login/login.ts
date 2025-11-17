import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../../services/api';
import { AuthService } from '../../services/auth';


import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-login',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatInputModule,
    MatFormFieldModule,
    MatButtonModule,
    MatCardModule
  ],
  templateUrl: './login.html',
  styleUrl: './login.scss',
})


export class Login {
  // 1. Declara la variable del formulario, pero NO la inicialices aquí
  loginForm; // <-- La declaramos vacía

  public serverError: string | null = null;
  public isNotVerifiedError: boolean = false; // Para mostrar el enlace "Reenviar"

  private emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
  // 2. Inyecta el "Constructor de Formularios" (FormBuilder)
  // 'fb' es solo un nombre corto para la variable 'FormBuilder'
  constructor(
    private fb: FormBuilder,
    private apiService: Api,
    private router: Router,
    private authService: AuthService

  ) {

    // 3. ¡AHORA SÍ! Crea el formulario DENTRO del constructor,
    //    porque 'this.fb' ya existe.
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.pattern(this.emailPattern)]],
      password: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    // Limpia los errores si el usuario empieza a escribir
    this.loginForm.valueChanges.subscribe(() => {
      this.serverError = null;
      this.isNotVerifiedError = false;
    });
  }

  // 4. Un método que llamaremos cuando se envíe el formulario
  onSubmit() {
    if (this.loginForm.invalid) {
      return;
    }
    
    this.serverError = null;
    this.isNotVerifiedError = false; // (Esta variable ya no la usaremos)

    this.apiService.login(this.loginForm.value)
      .subscribe({
        next: (response) => {
          this.authService.saveToken(response.token);
          this.router.navigate(['/']);
        },
        
        // --- ¡LA NUEVA LÓGICA DE ERROR! ---
        error: (err) => {
          console.error('Error en el login:', err);
          
          let errorMessage = err.error || err.message;
          if (typeof err.error === 'object') {
            errorMessage = err.error.message || JSON.stringify(err.error);
          }

          // 1. ¿Es el error de "no verificado"?
          if (err.status === 400 && errorMessage.includes('verifica tu email')) {
            
            // --- ¡TU IDEA! (Redirección Automática) ---
            alert("Tu cuenta no está verificada. Te redirigimos a la página de verificación.");
            const email = this.loginForm.get('email')?.value;
            this.router.navigate(['/verify-email'], {
              queryParams: { email: email } 
            });
            // -----------------------------------------
            
          } else if (err.status === 401 || err.status === 403) {
            // Error de contraseña incorrecta
            this.serverError = "Usuario o Contraseña inválido";
          } else {
            // Otro error
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

  // --- ¡AÑADE ESTE NUEVO MÉTODO! ---
  /**
   * Navega a la página de verificación, pasando el email
   * que el usuario ya escribió en el formulario de login.
   */
  goToVerification(): void {
    const email = this.loginForm.get('email')?.value;
    if (email) {
      // Redirige a /verify-email?email=...
      this.router.navigate(['/verify-email'], { 
        queryParams: { email: email } 
      });
    } else {
      // Si el campo de email está vacío, solo redirige
      this.router.navigate(['/verify-email']);
    }
  }
}
