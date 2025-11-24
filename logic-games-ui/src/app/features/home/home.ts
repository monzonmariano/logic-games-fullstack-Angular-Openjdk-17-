import { Component, OnInit,OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Api } from '../../core/services/api';
import { Router } from '@angular/router';
import { AuthService } from '../../core/services/auth';
import { Observable, Subscription, interval } from 'rxjs';


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
export class Home implements OnInit , OnDestroy{

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

  // --- VARIABLES PARA SWIPE Y AUTOPLAY ---
  private touchStartX = 0;
  private touchEndX = 0;
  private autoPlaySubscription: Subscription | null = null;

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
    // INICIAR AUTO-PLAY AL ENTRAR
    this.startAutoPlay();
  }
  ngOnDestroy(): void {
    this.stopAutoPlay(); // ¡Importante! Limpiar timer al salir
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
    this.resetAutoPlay();
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

  // --- LÓGICA DE SWIPE (TÁCTIL) ---

  onTouchStart(e: TouchEvent) {
    this.touchStartX = e.changedTouches[0].screenX;
    this.stopAutoPlay(); // Pausar mientras el usuario toca
  }

  onTouchEnd(e: TouchEvent) {
    this.touchEndX = e.changedTouches[0].screenX;
    this.handleSwipe();
    this.startAutoPlay(); // Reanudar
  }

  private handleSwipe() {
    // Umbral mínimo (50px) para considerar que fue un swipe intencional
    const threshold = 50;
    
    if (this.touchEndX < this.touchStartX - threshold) {
      // Deslizó a la IZQUIERDA -> Siguiente
      this.nextGame();
    } else if (this.touchEndX > this.touchStartX + threshold) {
      // Deslizó a la DERECHA -> Anterior
      this.prevGame();
    }
  }

  // --- LÓGICA DE AUTO-PLAY ---

  private startAutoPlay() {
    if (this.autoPlaySubscription) return; // Ya está corriendo
    
    // Cambia cada 5 segundos (5000ms)
    this.autoPlaySubscription = interval(5000).subscribe(() => {
      this.nextGame();
    });
  }

  private stopAutoPlay() {
    if (this.autoPlaySubscription) {
      this.autoPlaySubscription.unsubscribe();
      this.autoPlaySubscription = null;
    }
  }

  public resetAutoPlay() {
    this.stopAutoPlay();
    this.startAutoPlay();
  }

  // Método para decidir qué clase CSS lleva cada carta
  getCardClass(index: number): string {
    const len = this.games.length;
    
    // Índice de la carta ANTERIOR (Izquierda)
    const prevIndex = (this.currentIndex - 1 + len) % len;
    
    // Índice de la carta SIGUIENTE (Derecha)
    const nextIndex = (this.currentIndex + 1) % len;

    if (index === this.currentIndex) {
      return 'card-center';
    } else if (index === prevIndex) {
      return 'card-left';
    } else if (index === nextIndex) {
      return 'card-right';
    } else {
      return 'card-hidden'; // Si hubiera más de 3 juegos
    }
  }
}