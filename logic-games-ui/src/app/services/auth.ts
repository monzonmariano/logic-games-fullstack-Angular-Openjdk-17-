import { Injectable } from '@angular/core';
import { jwtDecode } from 'jwt-decode';
import { BehaviorSubject } from 'rxjs';


@Injectable({
  providedIn: 'root',
})

export class AuthService {
  
   private tokenKey = 'auth-token';
   public currentUserEmail$ = new BehaviorSubject<string | null>(null);

   constructor(){
    // ¡COMPRUEBA EL LOGIN AL INICIAR!
    this.loadUserFromToken();
   }

   
  private loadUserFromToken(): void {
    const token = this.getToken();
    if (token) {
      try {
        // Decodifica el token
        const decodedToken: { sub: string } = jwtDecode(token);
        // 'sub' (subject) es donde Spring guarda el email
        this.currentUserEmail$.next(decodedToken.sub);
      } catch (error) {
        console.error("Token inválido, borrando...", error);
        this.logout(); // Si el token es basura, bórralo
      }
    } else {
      this.currentUserEmail$.next(null);
    }
  }
   /**
   * Guarda el token (carnet) en el armario (localStorage).
   */
 public saveToken(token: string): void {
    localStorage.setItem(this.tokenKey, token);
    // 6. ¡ACTUALIZA LA TUBERÍA AL LOGUEARSE!
    this.loadUserFromToken();
  }
 
  public getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  public logout(): void {
    localStorage.removeItem(this.tokenKey);
    // 7. ¡ACTUALIZA LA TUBERÍA AL CERRAR SESIÓN!
    this.currentUserEmail$.next(null);
  }
  
  public isLoggedIn(): boolean {
    return !!this.getToken();
  }
}