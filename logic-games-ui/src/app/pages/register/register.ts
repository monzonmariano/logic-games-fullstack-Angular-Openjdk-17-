import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule ,FormBuilder,Validators,AbstractControl} from '@angular/forms';
import { Router } from '@angular/router';
import { Api } from '../../services/api';

import { MatInputModule } from '@angular/material/input';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

// Es una simple función. Recibe el "formulario" como argumento.
function passwordMatchValidator(control: AbstractControl) {
  // Obtenemos los valores de los dos campos
  const password = control.get('password')?.value;
  const confirmPassword = control.get('confirmPassword')?.value;

  // Si las contraseñas no coinciden...
  if (password !== confirmPassword) {
    // ...devolvemos un objeto de error.
    return { passwordMismatch: true };
  } else {
    // ...si coinciden, no devolvemos nada (null).
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

export class Register {
   
    registerForm;
    
    private emailPattern = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,4}$/;
    // --- 1. ¡LA NUEVA REGLA (RegEx) PARA CONTRASEÑAS! ---
  // Desglose:
  // (?=.*[a-z]) -> "Debe tener al menos una minúscula"
  // (?=.*[A-Z]) -> "Debe tener al menos una mayúscula"
  // (?=.*\d) -> "Debe tener al menos un número"
  // (?=.*[$@!%*?&]) -> "Debe tener al menos un caracter especial"
  // [A-Za-z\d$@!%*?&] -> (Esto es opcional, define los caracteres permitidos)
  private passwordPattern = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[$@!%*?&]).{8,}$/;

    constructor(
      private fb: FormBuilder,
      private apiService: Api,
      private router: Router
    )
      
      {
      
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

    onSubmit() {
    // Si el formulario no es válido, no hagas nada.
    if (this.registerForm.invalid) {
      return;
    }

   // console.log("Enviando al backend:", this.registerForm.value);

    
    
    this.apiService.register(this.registerForm.value)
      .subscribe({ 
        
        // 3. 'next' es lo que pasa si todo va BIEN
        next: () => {
        
          alert('¡Usuario registrado exitosamente!');
          // 4. ¡Usa el "GPS" para enviar al usuario al login!
          this.router.navigate(['/login']); 
        },
        
        // 5. 'error' es lo que pasa si el backend da un error
        error: (err) => {
          console.error('Error en el registro:', err);
          // (Aquí podríamos mostrar un mensaje de error al usuario, 
          // ej: "El email ya está en uso")
        }
      });
  }
}
