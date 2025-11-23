import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Api } from '../../core/services/api';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { Observable } from 'rxjs';


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
      description: 'El clásico juego de lógica numérica.',
      imageUrl: 'url("images/sudoku-card.jpg")'
    },
    {
      id: 'wordsearch',
      title: 'Sopa de Letras',
      description: 'Encuentra las palabras ocultas.', // <--- ¡YA NO ES PRÓXIMAMENTE!
      imageUrl: 'url("images/wordsearch-card.jpg")'
    },
    {
      id: 'crossword',
      title: 'Palabras Cruzadas',
      description: 'Próximamente...', // Este se queda igual por ahora
      imageUrl: 'url("images/crossword-card.jpg")'
    }
  ];  

  public currentIndex = 0; // Tarjeta actual

  public userEmail$: Observable<string | null>;

  constructor(
    private apiService: Api,
    private router: Router,
    private authService: AuthService

  ) { this.userEmail$ = this.authService.currentUserEmail$; }

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
    // El módulo (%) hace que 2 + 1 = 3 -> 3 % 3 = 0 (vuelve al inicio)
    this.currentIndex = (this.currentIndex + 1) % this.games.length;
  }

  prevGame() {
    // (this.currentIndex - 1 + total) % total
    // 0 - 1 = -1 -> -1 + 3 = 2 -> 2 % 3 = 2 (salta al final)
    this.currentIndex = (this.currentIndex - 1 + this.games.length) % this.games.length;
  }

  jumpToGame(index: number) {
    this.currentIndex = index;
  }

  selectGame(game: any) {
    if (game.id === 'sudoku') {
      this.router.navigate(['/sudoku-lobby']);
    } 
    else if (game.id === 'wordsearch') {
      // ¡Ahora redirigimos al Lobby de Sopa de Letras!
      this.router.navigate(['/wordsearch-lobby']);
    } 
    else {
      alert('¡Este juego estará disponible próximamente!');
    }
  }
}