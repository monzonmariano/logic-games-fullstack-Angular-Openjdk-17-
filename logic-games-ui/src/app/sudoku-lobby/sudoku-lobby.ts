
import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { Api } from '../services/api';
import { GameStateService } from '../services/game-state';

import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';

@Component({
  selector: 'app-sudoku-lobby',
  imports: [
    CommonModule,
    RouterLink, // <-- Para el botón "Volver"
    MatButtonModule,
    MatCardModule,
    MatIconModule
  ],
  templateUrl: './sudoku-lobby.html',
  styleUrl: './sudoku-lobby.scss',
})
export class SudokuLobby {

 public isLoading = false; // Para mostrar un "cargando"
 

 constructor(
    private apiService: Api,
    private gameState: GameStateService,
    private router: Router
  ) {}

  playGame(difficulty: string, mode: string) {
    this.isLoading = true; // Muestra el "cargando"

    this.apiService.loadOrCreateSudokuGame(difficulty, mode).subscribe({
      next: (gameData) => {
        // ¡Guarda el juego en el "cerebro" central!
        this.gameState.setCurrentGame(gameData);
        // ¡Navega al tablero de juego!
        this.router.navigate(['/play']); // (Asumiendo que tu tablero es '/play')
      },
      error: (err) => {
        console.error("Error al cargar el juego", err);
        alert("No se pudo iniciar la partida. Intenta de nuevo.");
        this.isLoading = false;
      }
    });
  }

}
