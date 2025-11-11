import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})

export class AuthService {
  
   private tokenKey = 'auth-token';


   constructor(){}

   /**
   * Guarda el token (carnet) en el armario (localStorage).
   */
  public saveToken(token: string): void {
    // 1. Borra cualquier token viejo por si acaso
    localStorage.removeItem(this.tokenKey);
    // 2. Guarda el token nuevo
    localStorage.setItem(this.tokenKey, token);
  }
 
   /**
   * Coge el token del armario.
   * Devuelve el string del token, o 'null' si no hay ninguno.
   */
  public getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  /**
   * Cierra sesión: saca el token del armario.
   */
  public logout(): void {
    localStorage.removeItem(this.tokenKey);
  }
  
  /**
   * Revisa si el usuario está logueado (si tiene un carnet).
   * ¡Esto será súper útil pronto!
   */
  public isLoggedIn(): boolean {
    // '!!' es un truco de pro:
    // 1. getToken() devuelve un string (ej. "ey...") o null.
    // 2. !getToken() lo convierte a boolean (false o true).
    // 3. !!getToken() lo invierte de nuevo (true o false).
    // Es la forma más rápida de convertir "algo" o "nada" a true/false.
    return !!this.getToken();
  }

}
