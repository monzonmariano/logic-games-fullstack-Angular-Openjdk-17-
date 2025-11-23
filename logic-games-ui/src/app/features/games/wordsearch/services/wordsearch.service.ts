import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

// Modelos (Mismos nombres que en el Backend)
export interface WordSearchGame {
  id: number;
  state: string;      // 'IN_PROGRESS', 'COMPLETED'
  difficulty: string;
  gridSize: number;
  matrixString: string; // "ABCDE..."
  wordsToFind: string[];
  foundWords: string[];
  timeElapsedSeconds: number;
}

export interface WordValidationRequest {
  word: string;
}

// Nuevo DTO para enviar el progreso en bloque
export interface WordSearchSaveRequest {
  foundWords: string[];
  timeElapsedSeconds: number;
}

@Injectable({
  providedIn: 'root'
})

export class WordSearchService {

  private apiUrl = '/api/wordsearch'; // Nginx redirige esto al backend

  constructor(private http: HttpClient) { }

  /**
   * Carga o Crea una partida (GET /load-or-create)
   */
public loadOrCreateGame(difficulty: string, gameMode: string, theme: string): Observable<WordSearchGame> {
    return this.http.get<WordSearchGame>(`${this.apiUrl}/load-or-create`, {
      params: { 
        difficulty, 
        gameMode,
        theme 
      }
    });
  }

 /**
   * Guarda el progreso (Pausa) o Completa el juego
   */
  public saveGame(foundWords: string[], timeSeconds: number, isComplete: boolean): Observable<void> {
    const payload: WordSearchSaveRequest = {
      foundWords: foundWords,
      timeElapsedSeconds: timeSeconds
    };
    
    // Usamos un query param para indicar si ha terminado
    return this.http.post<void>(`${this.apiUrl}/save?complete=${isComplete}`, payload);
  }

  public failGame(): Observable<void> {
    return this.http.post<void>(`${this.apiUrl}/fail`, {});
  }
}

