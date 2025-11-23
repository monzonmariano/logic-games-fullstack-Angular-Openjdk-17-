import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  
  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken(); // Coge el "carnet"

  // 3. Revisa si el usuario tiene carnet
  if (!token) {
    // Si NO tiene token, deja pasar la petición (maleta)
    // tal como vino. (Ej. para /login o /register)
    return next(req);
   }
  
   // 4. ¡SÍ TIENE TOKEN!
  // Hay que "clonar" la maleta y añadirle el token.
  // Las peticiones son "inmutables", no puedes cambiarlas,
  // solo puedes clonarlas.
  const clonedRequest = req.clone({
    setHeaders: {
      // Adjunta el "carnet" en la cabecera (Header)
      Authorization: `Bearer ${token}`
    }
  });

  // 5. Envía la maleta "clonada" (con el token) al avión.
  return next(clonedRequest).pipe(
     
    // 'catchError' es una herramienta que se activa
    // SOLO si la API devuelve un error (ej. 403)
    catchError(err => {
      
      // 6. Comprueba si el error es de "autenticación"
      if (err.status === 401 || err.status === 403) {
        
        // ¡NUESTRO CARNET ES BASURA! (Caducado o inválido)
        console.error("¡Sesión caducada o inválida! Redirigiendo al login.", err);
        
        // 7. ¡LIMPIA LA SESIÓN!
        authService.logout(); // Borra el token caducado del "armario"
        
        // 8. ¡ECHA AL USUARIO!
        router.navigate(['/login']);
        
        // (Opcional: muestra un mensaje)
        alert('Tu sesión ha caducado. Por favor, inicia sesión de nuevo.');
      }
      
      // 9. Vuelve a lanzar el error, para que el componente (home.ts)
      // sepa que algo falló y muestre "Error al cargar datos...".
      return throwError(() => err);
    })
  );
    

  


};
