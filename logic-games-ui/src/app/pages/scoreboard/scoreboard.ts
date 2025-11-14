import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Api } from '../../services/api'; // El "Plomero"
import { ScoreboardEntry, SudokuGame } from '../../services/game-state'; // La interfaz
import { MatListModule } from '@angular/material/list'; // <-- ¡Para listas bonitas!
import { MatIconModule } from '@angular/material/icon'; // <-- ¡Para iconos!
import { RouterLink } from '@angular/router'; // <-- Para el botón "Volver"
import { Observable } from 'rxjs';

@Component({
  selector: 'app-scoreboard',
  standalone: true,
  // ¡Añade los nuevos módulos!
  imports: [CommonModule, MatListModule, MatIconModule, RouterLink],
  templateUrl: './scoreboard.html',
  styleUrl: './scoreboard.scss'
})
export class Scoreboard implements OnInit {

  // ¡Una tubería para guardar el historial!
  public scoreboard$: Observable<ScoreboardEntry[]>;

  constructor(private apiService: Api) {
    // Inicializa la tubería vacía
    this.scoreboard$ = new Observable<ScoreboardEntry[]>();
  }

  ngOnInit(): void {
    // ¡Llama al plomero para pedir el historial!
    this.scoreboard$ = this.apiService.getScoreboard();
  }

  // ¡Un "helper" para reutilizar nuestro formateador de tiempo!
  formatTime(totalSeconds: number): string {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;
    const paddedMinutes = String(minutes).padStart(2, '0');
    const paddedSeconds = String(seconds).padStart(2, '0');
    return `${paddedMinutes}:${paddedSeconds}`;
  }
}