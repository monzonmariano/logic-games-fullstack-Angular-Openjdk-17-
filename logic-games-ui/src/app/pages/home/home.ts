import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Api } from '../../services/api';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth';
import { Observable } from 'rxjs';
// ¡Importa el "cerebro" del estado del juego!
import { GameStateService } from '../../services/game-state';

// ¡Importa los módulos de Material para la UI!
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';


@Component({
  selector: 'app-home',
  standalone: true,
  // ¡Añade los módulos de Material!
  imports: [
    CommonModule,
    MatButtonModule,
    MatCardModule,
    MatSelectModule,
    MatFormFieldModule,
    MatIconModule
  ],
  templateUrl: './home.html',
  styleUrl: './home.scss',
})
export class Home implements OnInit {

  public secureMessage = 'Cargando datos secretos...';

  // ¡La lista de juegos!
 public games = [
    { 
      id: 'sudoku', 
      title: 'Sudoku', 
      description: 'El clásico juego de lógica...',
      // ¡SIN "assets/"!
      imageUrl: 'url("images/sudoku-card.jpg")' 
    },
    { 
      id: 'wordsearch', 
      title: 'Sopa de Letras', 
      description: 'Próximamente...',
      imageUrl: 'url("images/wordsearch-card.jpg")'
    },
    { 
      id: 'crossword', 
      title: 'Palabras Cruzadas', 
      description: 'Próximamente...',
      imageUrl: 'url("images/crossword-card.jpg")'
    }
  ];

  public currentIndex = 0; // Tarjeta actual

  public userEmail$: Observable<string | null>;

  constructor(
    private apiService: Api,
    private router: Router,
    private authService: AuthService
  
  ) {this.userEmail$ = this.authService.currentUserEmail$; }

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

  // --- Lógica del Carrusel ---
  nextGame() {
    if (this.currentIndex < this.games.length - 1) {
      this.currentIndex++;
    }
  }

  prevGame() {
    if (this.currentIndex > 0) {
      this.currentIndex--;
    }
  }

  jumpToGame(index: number) {
    this.currentIndex = index;
  }
  
  selectGame(game: any) {
    if (game.id === 'sudoku') {
      // ¡YA NO jugamos! Ahora navegamos a una NUEVA
      // pantalla de "lobby" específica para Sudoku.
      // (Esta página la crearemos en el siguiente paso)
      this.router.navigate(['/sudoku-lobby']);
    } else {
      alert('¡Este juego estará disponible próximamente!');
    }
  }
}