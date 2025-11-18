import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { ScoreboardEntry, SudokuGame } from './game-state'; 

// --- INTERFACES DE AUTENTICACIÓN ---

export interface HelloResponse {
  message: string;
}
export interface RegisterRequest {
  email?: string | null;
  password?: string | null;
  confirmPassword?: string | null;
}
export interface AuthenticationRequest {
  email?: string | null;
  password?: string | null;
}
export interface AuthenticationResponse {
  token: string;
}
export interface ResetPasswordRequest {
  token: string;
  newPassword?: string | null;
}

export interface VerifyEmailRequest {
  email: string;
  otpCode: string;
}
// --- INTERFACES PARA EL JUEGO! ---

// Para POST /api/sudoku/save (coincide con SaveGameRequest.java)
export interface SaveGameRequest {
  boardString: string;
  timeElapsedSeconds: number;
}

// Para POST /api/sudoku/complete (coincide con SudokuSolutionRequest.java)
export interface SudokuSolutionRequest {
  boardString: string;
  timeElapsedSeconds: number;
}

export interface ResetPasswordWithCodeRequest {
  email: string;
  otpCode: string;
  newPassword?: string; // (o string | null si prefieres)
}

@Injectable({
  providedIn: 'root'
})
export class Api { 
  
  // private apiUrl = 'http://localhost:8080/api';
  private apiUrl = '/api'; 
  constructor(private http: HttpClient) { }

  // --- MÉTODOS DE AUTENTICACIÓN ---
  
  public getHelloMessage(): Observable<HelloResponse> {
    return this.http.get<HelloResponse>(`${this.apiUrl}/Hello`);
  }

  public register(request: RegisterRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/auth/register`, request, { 
      responseType: 'text' 
    });
  }

  public login(request: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.apiUrl}/auth/login`, request);
  }
  
  public getSecureData(): Observable<HelloResponse> {
    return this.http.get<HelloResponse>(`${this.apiUrl}/secure-data`);
  }

  public requestPasswordReset(email: string): Observable<string> {
    const body = { email: email }; 
    return this.http.post(`${this.apiUrl}/auth/forgot-password`, body, {
      responseType: 'text'
    });
  }
  
  public resetPassword(request: ResetPasswordRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/auth/reset-password`, request, {
      responseType: 'text'
    });
  }
  
  // (Para la página de verificación)
  public verifyEmail(request: VerifyEmailRequest): Observable<string> {
    // Llama al endpoint que creamos en AuthController
    return this.http.post(`${this.apiUrl}/auth/verify-email`, request, { 
      responseType: 'text' 
    });
  }

  public resendVerificationCode(email: string): Observable<string> {
    const body = { email: email };
    // Llama al endpoint que creamos en AuthController
    return this.http.post(`${this.apiUrl}/auth/resend-verification`, body, {
      responseType: 'text'
    });
  }

  // (Para la página de "clic en enlace")
  public verifyEmailLink(token: string): Observable<string> {
    // Llama al endpoint GET que creamos en AuthController
    return this.http.get(`${this.apiUrl}/auth/verify-email-link`, {
      params: { token: token },
      responseType: 'text'
    });
  }
 

  public resetPasswordWithCode(request: ResetPasswordWithCodeRequest): Observable<string> {
    return this.http.post(`${this.apiUrl}/auth/reset-password-code`, request, { // endpoint nuevo
      responseType: 'text'
    });
  }
  // --- MÉTODOS DE SUDOKU ---
   
  
  public loadOrCreateSudokuGame(difficulty: string , mode : string): Observable<SudokuGame> {
    return this.http.get<SudokuGame>(`${this.apiUrl}/sudoku/load-or-create`, {
      params: {
        difficulty: difficulty,
        gameMode: mode
      }
    });
  }
  
  // --- MÉTODO PARA GUARDAR PROGRESO! ---
  public saveGameProgress(request: SaveGameRequest): Observable<void> {
    // Llama a POST /api/sudoku/save
    return this.http.post<void>(`${this.apiUrl}/sudoku/save`, request);
  }

  // --- MÉTODO PARA COMPROBAR SOLUCIÓN! ---
  public completeGame(request: SudokuSolutionRequest): Observable<boolean> {
    // Llama a POST /api/sudoku/complete
    // Espera una respuesta 'true' o 'false'
    return this.http.post<boolean>(`${this.apiUrl}/sudoku/complete`, request);
  }
  // --- ¡MÉTODO PARA MARCAR "FALLO"! ---
  public failGame(): Observable<void> {
    // Llama a POST /api/sudoku/fail
    // No envía cuerpo y no espera respuesta (void)
    return this.http.post<void>(`${this.apiUrl}/sudoku/fail`, {});
  }
  // --- MÉTODO PARA OBTENER EL SCOREBOARD! ---
  public getScoreboard(): Observable<ScoreboardEntry[]> {
    // Llama a GET /api/sudoku/scoreboard
    // Espera un array de objetos SudokuGame
    return this.http.get<ScoreboardEntry[]>(`${this.apiUrl}/sudoku/scoreboard`);
  }
}