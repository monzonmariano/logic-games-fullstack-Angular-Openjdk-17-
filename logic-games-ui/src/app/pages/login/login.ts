import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { Router,RouterLink } from '@angular/router';
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

  // 4. Un método que llamaremos cuando se envíe el formulario
  onSubmit() {

    // ¡Asegúrate de que el formulario es válido!
    if (this.loginForm.invalid) {
      return;
    }

    this.apiService.login(this.loginForm.value)
      .subscribe({
        next: (response) => {
          this.authService.saveToken(response.token);
          alert("Te has logueado exitosamente!");

          // 4. Lo enviamos a la página principal.
          this.router.navigate(['/']); // Redirige a la raíz

        },
        // 5. 'error' es lo que pasa si el backend da un error
        error: (err) => {
          console.error('Error en el login:', err);
          alert("Usiario o Contraseña invalido");
        }

      });


  }
}
