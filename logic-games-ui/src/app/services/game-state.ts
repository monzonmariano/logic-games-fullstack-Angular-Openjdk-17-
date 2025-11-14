import { Injectable } from '@angular/core';
import { BehaviorSubject } from 'rxjs';

// Necesitamos una "interfaz" (contrato) que coincida
// con el JSON que envía nuestro backend (SudokuGame.java).
export interface SudokuGame {
  id: number;
  // No necesitamos el 'user' en el frontend
  boardString: string;
  solutionString: string;
  difficulty: string;
  state: string;
  gameMode: string;
  timeLimitSeconds: number;
  timeElapsedSeconds: number;
  lastUpdatedAt: string; // (las fechas se convierten en strings)
}




// Esta interfaz coincide con ScoreboardEntryDTO.java
export interface ScoreboardEntry {
  id: number;
  difficulty: string;
  timeElapsedSeconds: number;
  lastUpdatedAt: string;
  userEmail: string; // <-- ¡El campo que el backend está enviando!
}
 
@Injectable({
  providedIn: 'root'
})

export class GameStateService {

  // 1. Una "tubería" que guarda la partida actual.
  // Empieza estando "vacía" (null).
  private currentGame$ = new BehaviorSubject<SudokuGame | null>(null);

  constructor() { }

  // 2. Método para "guardar" la partida en la memoria
  public setCurrentGame(game: SudokuGame): void {
    this.currentGame$.next(game);
  }

  // 3. Método para que el tablero "pida" la partida
  // (Devolvemos la "tubería" para que se suscriban)
  public getCurrentGame(): BehaviorSubject<SudokuGame | null> {
    return this.currentGame$;
  }
}
