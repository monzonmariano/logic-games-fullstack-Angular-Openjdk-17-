import { CanActivateFn,Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';

export const authGuard: CanActivateFn = (route, state) => {
  
  // 2. "Inyecta" (pide) las herramientas que necesitamos
  // inject() es la forma moderna de pedir herramientas dentro de un guardia
  const authService = inject(AuthService);
  const router = inject(Router);
  
  // 3. ¡La Lógica del Guardia!
  if (authService.isLoggedIn()) {
    // Tiene carnet (token), puede pasar.
    return true;
  }
   
  // 4. No tiene carnet.
  // Lo enviamos al login y bloqueamos el acceso.
  console.log("Acceso denegado - Redirigiendo al login");
  router.navigate(['/login']);
  return false; 

  
};