import { Component, OnDestroy, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { GameStateService, SudokuGame } from '../../../../../core/services/game-state';
import { Router } from '@angular/router';
import { Subscription, interval } from 'rxjs';
import { FormBuilder, FormControl, FormGroup, ReactiveFormsModule } from '@angular/forms';
import { Api, SaveGameRequest, SudokuSolutionRequest } from '../../../../../core/services/api';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { Numpad } from '../../components/numpad/numpad';

export interface SudokuCell {
  value: number; // 0 si está vacía, 1-9 si tiene un número
  isOriginal: boolean; // 'true' si es un número original (no se puede editar)
}

@Component({
  selector: 'app-sudoku-board',
  imports: [CommonModule, ReactiveFormsModule, MatButtonModule, MatIconModule, MatButtonModule, Numpad], // ¡CommonModule nos da @for y @if!
  templateUrl: './sudoku-board.html',
  styleUrl: './sudoku-board.scss',
})
export class SudokuBoard implements OnInit, OnDestroy {

  // A. Modelo de Vista: Un array 9x9 para dibujar el grid
  public boardView: SudokuCell[][] = [];

  // B. Modelo de Datos: Un FormGroup para guardar los inputs del usuario
  public boardForm: FormGroup = new FormGroup({}); // Lo inicializamos vacío

  public difficulty: string = '';
  private gameSubscription: Subscription | undefined;

  // --- El "inventario" de celdas con error ---
  public invalidCells = new Set<string>();

  // --- Guardamos la solución aquí ---
  private solutionString: string = '';
  // --- ¡NUEVO! Un mensaje para el usuario ---
  public gameMessage: string = '';
  private timerSubscription: Subscription | undefined;
  // --- ¡LÓGICA DEL TIMER ACTUALIZADA! ---
  public timeElapsed: number = 0; // Segundos JUGADOS (para guardar)
  public timeRemaining: number = 0; // Segundos RESTANTES (para mostrar)
  public timeDisplay: string = '00:00'; // El string MM:SS
  public gameMode: string = 'FREE';
  public isTimeCritical: boolean = false; // ¡Para el titileo!
  public isGameOver: boolean = false;
  private isSaving: boolean = false;

  private messageTimer: any;

  // --- VARIABLES DE ESTADO PARA EL POP-UP! ---
  public isNumpadOpen: boolean = false;
  public numpadPosition = { x: 0, y: 0 };
  public activeCellKey: string | null = null;

  constructor(
    private gameState: GameStateService,
    private router: Router,
    private fb: FormBuilder,
    private apiService: Api
  ) { }

  ngOnInit(): void {
    this.gameSubscription = this.gameState.getCurrentGame().subscribe(gameData => {
      if (!gameData) {
        this.router.navigate(['/']);
        return;
      }

      this.difficulty = gameData.difficulty;
      this.boardView = this.parseBoardView(gameData.boardString);
      this.boardForm = this.createBoardForm(this.boardView);
      this.solutionString = gameData.solutionString;

      // --- ¡LÓGICA DE ARRANQUE DEL TIMER! ---
      this.gameMode = gameData.gameMode;
      this.timeElapsed = gameData.timeElapsedSeconds; // Carga el tiempo jugado

      if (this.gameMode === 'TIMED') {
        const timeLimit = gameData.timeLimitSeconds;
        this.timeRemaining = timeLimit - this.timeElapsed; // Calcula el restante
        this.timeDisplay = this.formatTime(this.timeRemaining); // Muestra el restante
        this.startTimer(); // ¡Inicia la cuenta atrás!
      } else {
        this.timeDisplay = this.formatTime(this.timeElapsed); // Muestra el cronómetro (hacia arriba)
        this.startStopwatch(); // ¡Inicia el cronómetro!
      }

      this.validateAllCells();
    });
  }

  ngOnDestroy(): void {
    this.gameSubscription?.unsubscribe();
    this.timerSubscription?.unsubscribe(); // ¡Importante! Detiene el timer al salir
    // Si el usuario "abandona" (NO está guardando)
    // Y la partida NO ha terminado (ni ganada ni perdida)...
    if (!this.isSaving && !this.isGameOver) {
      console.log("¡Abandonando partida! Marcando como FAILED.");

      // ...llama a la API para "cancelar" la partida.
      // ¡Reutilizamos tu endpoint 'failGame'!
      this.apiService.failGame().subscribe({
        next: () => console.log("Partida en progreso borrada."),
        error: (err) => console.error("Error al borrar partida", err)
      });
    }
    if (this.messageTimer) clearTimeout(this.messageTimer);
  }

  // --- ¡TEMPORIZADOR "HACIA ARRIBA" (MODO LIBRE)! ---
  private startStopwatch(): void {
    this.timerSubscription = interval(1000).subscribe(() => {
      this.timeElapsed++; // Solo cuenta hacia arriba
      this.timeDisplay = this.formatTime(this.timeElapsed);
    });
  }
  // --- ¡TEMPORIZADOR "HACIA ABAJO" (MODO TIEMPO)! ---
  private startTimer(): void {
    this.timerSubscription = interval(1000).subscribe(() => {

      if (this.timeRemaining > 0) {
        this.timeRemaining--; // Cuenta hacia abajo
        this.timeElapsed++;   // Sigue contando lo jugado (para guardar)
        this.timeDisplay = this.formatTime(this.timeRemaining);

        // ¡Tu idea del titileo!
        if (this.timeRemaining <= 10) {
          this.isTimeCritical = true;
        }

      } else {
        // ¡SE ACABÓ EL TIEMPO!
        this.isTimeCritical = false;
        this.timerSubscription?.unsubscribe();
        this.gameMessage = "¡SE ACABÓ EL TIEMPO! 😥 Has perdido.";
        this.boardForm.disable(); // Bloquea el tablero
        this.isGameOver = true; // 

        this.apiService.failGame().subscribe({
          next: () => console.log("Partida marcada como FAILED en el backend."),
          error: (err) => console.error("Error al marcar la partida como FAILED", err)
        });
      }
    });
  }
  // --- MÉTODO "HELPER"! ---
  // Convierte segundos (ej. 125) en un string "MM:SS" (ej. "02:05")
  private formatTime(totalSeconds: number): string {
    const minutes = Math.floor(totalSeconds / 60);
    const seconds = totalSeconds % 60;

    const paddedMinutes = String(minutes).padStart(2, '0');
    const paddedSeconds = String(seconds).padStart(2, '0');

    return `${paddedMinutes}:${paddedSeconds}`;
  }
  /**
   * ¡El TRADUCTOR!
   * Convierte el string "53007..." en una cuadrícula 9x9.
   */
  /**
   * Crea el Modelo de Vista (boardView)
   * 
   */
  private parseBoardView(boardString: string): SudokuCell[][] {
    const grid: SudokuCell[][] = [];
    let index = 0;
    for (let i = 0; i < 9; i++) {
      const row: SudokuCell[] = [];
      for (let j = 0; j < 9; j++) {
        const numValue = parseInt(boardString[index], 10);
        row.push({
          value: numValue,
          isOriginal: numValue !== 0
        });
        index++;
      }
      grid.push(row);
    }
    return grid;
  }

  /**
   * Crea el Modelo de Formulario (boardForm)
   * basado en el Modelo de Vista.
   */
  private createBoardForm(boardView: SudokuCell[][]): FormGroup {
    const controls: { [key: string]: FormControl } = {};

    for (let i = 0; i < 9; i++) {
      for (let j = 0; j < 9; j++) {
        const cell = boardView[i][j];
        const key = `${i}-${j}`; // La "llave" será "0-0", "0-1", etc.

        if (cell.isOriginal) {
          // Si es original: crea un control "deshabilitado" con el valor
          controls[key] = this.fb.control({ value: cell.value, disabled: true });
        } else {
          // Si está vacía: crea un control "habilitado" y vacío
          controls[key] = this.fb.control('');
        }
      }
    }
    return this.fb.group(controls);
  }

  // --- Se llama desde el (input) del HTML ---
  public onCellInput(event: any, row: number, col: number): void {
    const inputElement = event.target as HTMLInputElement;
    let value = inputElement.value;

    // 1. Limpia el input (solo 1-9)
    if (value.length > 1) {
      value = value[value.length - 1]; // Coge solo el último dígito
    }
    if (!/^[1-9]$/.test(value)) {
      value = ''; // Si no es 1-9 (ej. es '0'), bórralo
    }

    // 2. Actualiza el valor en el "robot" del formulario
    const cellKey = `${row}-${col}`;
    this.boardForm.get(cellKey)?.setValue(value, { emitEvent: false });

    // 3. ¡Valida!
    this.validateAllCells();
  }

  // --- Revisa todo el tablero en busca de errores ---
  private validateAllCells(): void {
    this.invalidCells.clear(); // Limpia errores viejos
    const boardValues = this.boardForm.getRawValue(); // Obtiene TODOS los valores (incluyendo deshabilitados)

    for (let i = 0; i < 9; i++) {
      for (let j = 0; j < 9; j++) {
        const key = `${i}-${j}`;
        const value = boardValues[key];

        if (value) { // Si la celda no está vacía
          if (
            this.isInvalidInRow(boardValues, i, j, value) ||
            this.isInvalidInCol(boardValues, i, j, value) ||
            this.isInvalidInBox(boardValues, i, j, value)
          ) {
            this.invalidCells.add(key); // ¡Añade al "inventario" de errores!
          }
        }
      }
    }
  }
  // --- 3 FUNCIONES "AYUDANTES" DE LÓGICA ---
  private isInvalidInRow(board: any, row: number, col: number, value: any): boolean {
    for (let j = 0; j < 9; j++) {
      if (j !== col && board[`${row}-${j}`] == value) {
        return true; // ¡Error! Encontrado en la misma fila
      }
    }
    return false;
  }

  private isInvalidInCol(board: any, row: number, col: number, value: any): boolean {
    for (let i = 0; i < 9; i++) {
      if (i !== row && board[`${i}-${col}`] == value) {
        return true; // ¡Error! Encontrado en la misma columna
      }
    }
    return false;
  }

  private isInvalidInBox(board: any, row: number, col: number, value: any): boolean {
    const startRow = Math.floor(row / 3) * 3;
    const startCol = Math.floor(col / 3) * 3;
    for (let i = 0; i < 3; i++) {
      for (let j = 0; j < 3; j++) {
        const r = startRow + i;
        const c = startCol + j;
        if ((r !== row || c !== col) && board[`${r}-${c}`] == value) {
          return true; // ¡Error! Encontrado en el mismo bloque 3x3
        }
      }
    }
    return false;
  }


  /**
   *Convierte el objeto del formulario (ej. {"0-0": 5, "0-1": 3...})
   * en un string de 81 caracteres (ej. "53...").
   */
  private convertBoardToString(boardValues: any): string {
    let boardString = "";
    for (let i = 0; i < 9; i++) {
      for (let j = 0; j < 9; j++) {
        const key = `${i}-${j}`;
        // Si la celda está deshabilitada (es original), usa el valor de 'boardView'
        // Si no, usa el valor del formulario (lo que escribió el usuario)
        const cell = this.boardView[i][j];
        if (cell.isOriginal) {
          boardString += cell.value;
        } else {
          boardString += boardValues[key] || 0; // Si está vacío "", pon 0
        }
      }
    }
    return boardString;
  }

  goBackToHome(): void {
    // (Podríamos preguntar "¿Estás seguro? Perderás el progreso no guardado")
    // Por ahora, solo navegamos.
    this.router.navigate(['/sudoku-lobby']);
  }

  // ---  ---
  checkSolution(): void {
    if (this.invalidCells.size > 0) {
      // Usa la nueva función (3 segundos)
      this.showMessage("¡Revisa las celdas rojas! Tienes errores.", 3000); 
      return;
    }

    const userSolutionString = this.convertBoardToString(this.boardForm.getRawValue());

    if (userSolutionString !== this.solutionString) {
      // Usa la nueva función
      this.showMessage("La solución aún no es correcta. ¡Sigue intentando!", 3000);
      return;
    }

    this.gameMessage = "¡Solución correcta! Comprobando con el servidor...";
    // (Aquí no usamos timeout todavía porque estamos esperando al servidor)

    const request: SudokuSolutionRequest = {
      boardString: userSolutionString,
      timeElapsedSeconds: this.timeElapsed
    };

    this.apiService.completeGame(request).subscribe({
      next: (didWin) => {
        if (didWin) {
          // ¡SI GANA, LO DEJAMOS MÁS TIEMPO! (Ej. 5 o 10 segundos, o fijo)
          // Como el juego termina, el botón se desactiva, así que no molesta tanto.
          this.showMessage("¡¡FELICIDADES, HAS GANADO!! 🏆", 6000); 
          
          this.timerSubscription?.unsubscribe();
          this.boardForm.disable();
          this.isGameOver = true;
        } else {
           this.showMessage("Error del servidor. Intenta de nuevo.", 3000);
        }
      },
      error: (err) => {
        console.error("Error al comprobar", err);
        this.showMessage("Error al conectar con el servidor.", 3000);
      }
    });
  }
  // --- 4. ¡NUEVO MÉTODO saveAndQuit()! ---
  saveAndQuit(): void {
    this.isSaving = true;
    this.timerSubscription?.unsubscribe();

    const boardString = this.convertBoardToString(this.boardForm.getRawValue());

    const request: SaveGameRequest = {
      boardString: boardString,
      timeElapsedSeconds: this.timeElapsed
    };

    this.apiService.saveGameProgress(request).subscribe({
      next: () => {
        console.log("¡Partida guardada!");
        this.router.navigate(['/sudoku-lobby']); // Envía al usuario al sudoku lobby  ota ve xD
      },
      error: (err) => {
        console.error("Error al guardar la partida", err);
        alert("No se pudo guardar la partida.");
      }
    });
  }

  // Guarda la celda que el usuario acaba de tocar
  public onCellClick(event: MouseEvent, row: number, col: number): void {
    const cellKey = `${row}-${col}`;
    const control = this.boardForm.get(cellKey);

    // 1. Definimos el tamaño de tu pop-up (basado en el CSS que haremos)
    const popupWidth = 180; // 3 botones de 60px
    const popupHeight = 240; // 4 filas de 60px
    const margin = 10; // 10px de "aire" con el borde
    // En móvil, desplaza el popup 60px hacia ARRIBA del dedo
    const touchOffset = (window.innerWidth < 600) ? 60 : 0;
    let top = event.clientY - (popupHeight / 2) - touchOffset;
    // 2. Usamos una medida de pantalla más precisa
    const screenWidth = document.documentElement.clientWidth;
    const screenHeight = document.documentElement.clientHeight;

    if (control && control.enabled) {
      this.activeCellKey = cellKey;


      // 3. Centrado (igual que antes)
      let left = event.clientX - (popupWidth / 2);
      let top = event.clientY - (popupHeight / 2);

      // 4. Detección de bordes (más robusta)

      // Borde derecho
      if (left + popupWidth > screenWidth - margin) {
        left = screenWidth - popupWidth - margin;
      }
      // Borde izquierdo
      if (left < margin) {
        left = margin;
      }
      // Borde inferior
      if (top + popupHeight > screenHeight - margin) {
        top = screenHeight - popupHeight - margin;
      }
      // Borde superior
      if (top < margin) {
        top = margin;
      }

      this.numpadPosition = { x: left, y: top };
      this.isNumpadOpen = true;
    }
  }
 

  private showMessage(message: string, duration: number = 3000): void {
    // A. Si ya había un timer corriendo (mensaje anterior), cancélalo
    if (this.messageTimer) {
      clearTimeout(this.messageTimer);
    }

    // B. Muestra el mensaje
    this.gameMessage = message;

    // C. Inicia la cuenta atrás para borrarlo
    this.messageTimer = setTimeout(() => {
      this.gameMessage = ''; // ¡Borra el mensaje! (Angular lo quita del DOM)
      this.messageTimer = null;
    }, duration);
  }
  public onNumpadInput(value: number | null): void {
    if (!this.activeCellKey) {
      return;
    }

    const control = this.boardForm.get(this.activeCellKey);

    if (control && control.enabled) {
      control.setValue(value ? value.toString() : '');
      this.validateAllCells(); //
    }

    // ¡Cierra el pop-up después de seleccionar!
    this.isNumpadOpen = false;
    this.activeCellKey = null; // Limpia la celda activa
  }

}
