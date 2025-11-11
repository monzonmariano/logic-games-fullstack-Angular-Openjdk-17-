import { Component ,OnInit} from '@angular/core';
import { CommonModule } from '@angular/common';
import { Api } from '../../services/api';
import { Router } from '@angular/router';

// ¡Importa el "cerebro" del estado del juego!
import { GameStateService } from '../../services/game-state'; 

// ¡Importa los módulos de Material para la UI!
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';

@Component({
  selector: 'app-home',
  // ¡Añade los módulos de Material!
  imports: [CommonModule, MatButtonModule, MatCardModule], 
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {
   
  public secureMessage = 'Cargando datos secretos...';

   constructor(
    private apiService: Api,
    private router: Router,
    private gameState: GameStateService // ¡Inyecta el estado del juego!
  ) {}

  ngOnInit(): void {
    // Esto está perfecto, carga tu mensaje secreto
    this.apiService.getSecureData().subscribe({
      next: (response) => {
        this.secureMessage = response.message;
      },
      error: (err) => {
        console.error("Error al cargar datos seguros", err);
        this.secureMessage = "¡Error! No pudimos cargar los datos.";
      }
    });
  }

  // ¡El método que llamarán tus botones!
  playGame(difficulty: string, mode: string) {
    console.log(`Solicitando partida: ${difficulty} - ${mode}`);
    
    // Llama al "plomero" (api.ts)
    this.apiService.loadOrCreateSudokuGame(difficulty, mode).subscribe({
      next: (gameData) => {
        console.log("Datos del juego recibidos:", gameData);
        
        // 1. Guarda los datos en la "memoria"
        this.gameState.setCurrentGame(gameData);
        
        // 2. Navega a la página del tablero
        this.router.navigate(['/play/sudoku']);
      },
      error: (err) => {
        console.error("Error al cargar el juego", err);
        alert("Error al cargar el juego. Intenta de nuevo.");
      }
    });
  }
}