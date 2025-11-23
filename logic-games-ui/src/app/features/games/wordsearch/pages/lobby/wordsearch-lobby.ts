import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { WordSearchService } from '../../services/wordsearch.service';

import { MatSelectModule } from '@angular/material/select';
import { MatFormFieldModule } from '@angular/material/form-field'; 


@Component({
  selector: 'app-wordsearch-lobby',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatButtonModule, 
    MatCardModule, 
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,      
    MatFormFieldModule
  ],
  // --- ¡AQUÍ ESTÁ EL CAMBIO! ---
  templateUrl: './wordsearch-lobby.html', // Apunta al archivo HTML
  styleUrl: './wordsearch-lobby.scss'     // Apunta al archivo SCSS
})
export class WordSearchLobby {
  public isLoading = false;
  
  // 1. Lista de Temas (Coincide con tu Backend)
  public themes = [
    { id: 'GENERAL', label: '🎲 General (Diccionario)',icon:"shuffle" },
    { id: 'TECH', label: '💻 Tecnología',icon:"computer" },
    { id: 'ANIMALS', label: '🦁 Animales',icon:"pets" },
    { id: 'COUNTRIES', label: '🌍 Países',icon:"public" }
  ];

  // 2. Tema seleccionado por defecto
  public selectedTheme: string = 'GENERAL';

  constructor(
    private wsService: WordSearchService,
    private router: Router
  ) {}

  

  startGame(difficulty: string, mode: string) {
    this.isLoading = true;
    // Usamos this.selectedTheme directamente
    this.wsService.loadOrCreateGame(difficulty, mode, this.selectedTheme).subscribe({
      next: (game) => {
        this.router.navigate(['/play/wordsearch'], { state: { gameData: game } });
      },
      error: (err) => {
        console.error(err);
        this.isLoading = false;
        alert('Error al iniciar el juego.');
      }
    });
  }
}