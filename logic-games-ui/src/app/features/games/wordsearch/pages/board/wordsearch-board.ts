import { Component, OnInit, HostListener, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { WordSearchService, WordSearchGame } from '../../services/wordsearch.service';
import { Subscription, interval } from 'rxjs';


interface Cell {
    char: string;
    row: number;
    col: number;
    selected: boolean; // Selección temporal (azul)
    found: boolean;    // Palabra encontrada (verde)
}

@Component({
    selector: 'app-wordsearch-board',
    standalone: true,
    imports: [CommonModule, MatButtonModule, MatIconModule, MatCardModule],
    templateUrl: './wordsearch-board.html',
    styleUrl: './wordsearch-board.scss'
})
export class WordSearchBoard implements OnInit, OnDestroy {

    public game: WordSearchGame | null = null;
    public grid: Cell[][] = [];
    public isLoading = true;

    // Estado de la selección
    public isSelecting = false;
    private startCell: { r: number, c: number } | null = null;
    public currentSelection: Set<string> = new Set(); // IDs de celdas seleccionadas "r-c"

    // VARIABLES DEL TEMPORIZADOR
    private timerSubscription: Subscription | null = null;
    public timeDisplay: string = "00:00";
    public timeRemaining: number = 0;
    public isTimeCritical: boolean = false;
    public isGameOver: boolean = false;
    public gameMessage: string = ""; // Para el mensaje de "Ganaste"

    constructor(
        private wsService: WordSearchService,
        private router: Router
    ) { }

    ngOnInit() {
        // 1. Intentar recuperar estado pasado por el Lobby
        const navState = history.state;
        if (navState && navState.gameData) {
            this.initGame(navState.gameData);
        } else {
            // Si recargan la página, volver al lobby (o recargar API)
            this.router.navigate(['/wordsearch-lobby']);
        }
    }

    ngOnDestroy() {
        this.timerSubscription?.unsubscribe();
        // Si se va sin terminar, guardamos (lógica de abandono opcional)
    }

    private initGame(gameData: WordSearchGame) {
        this.game = gameData;
        this.grid = [];

        // 2. Parsear la "matrixString" (string largo) a Grilla 2D
        let charIndex = 0;
        for (let r = 0; r < gameData.gridSize; r++) {
            const row: Cell[] = [];
            for (let c = 0; c < gameData.gridSize; c++) {
                row.push({
                    char: gameData.matrixString[charIndex],
                    row: r,
                    col: c,
                    selected: false,
                    found: false
                    // Nota: Aquí podrías chequear si la celda es parte de 'foundWords' 
                    // recuperados del backend para repintar partidas guardadas.
                });
                charIndex++;
            }
            this.grid.push(row);
        }
        this.isLoading = false;
        this.setupTimer();
    }

    private setupTimer() {
        if (!this.game) return;

        const elapsed = this.game.timeElapsedSeconds || 0;

        if (this.game.difficulty === 'TIMED' || (this.game as any).gameMode === 'TIMED') {
            // MODO TIEMPO (Cuenta Atrás)
            const limit = (this.game as any).timeLimitSeconds || 600;
            this.timeRemaining = limit - elapsed;
            this.timeDisplay = this.formatTime(this.timeRemaining);

            this.timerSubscription = interval(1000).subscribe(() => {
                if (this.timeRemaining > 0) {
                    this.timeRemaining--;
                    this.game!.timeElapsedSeconds++; // Seguimos contando el total jugado
                    this.timeDisplay = this.formatTime(this.timeRemaining);

                    if (this.timeRemaining < 30) this.isTimeCritical = true;
                } else {
                    this.handleGameOver("¡Se acabó el tiempo! ⏳");
                }
            });

        } else {
            // MODO ZEN (Cronómetro)
            this.timeDisplay = this.formatTime(elapsed);
            this.timerSubscription = interval(1000).subscribe(() => {
                this.game!.timeElapsedSeconds++;
                this.timeDisplay = this.formatTime(this.game!.timeElapsedSeconds);
            });
        }
    }

    private formatTime(seconds: number): string {
        const m = Math.floor(seconds / 60).toString().padStart(2, '0');
        const s = (seconds % 60).toString().padStart(2, '0');
        return `${m}:${s}`;
    }

    // --- EVENTOS DEL RATÓN (MOUSE) ---

    onMouseDown(r: number, c: number) {
        this.isSelecting = true;
        this.startCell = { r, c };
        this.updateSelection(r, c); // Selecciona la primera celda
    }

    onMouseEnter(r: number, c: number) {
        if (this.isSelecting) {
            this.updateSelection(r, c);
        }
    }

    // Escucha global por si sueltan el clic fuera del tablero
    @HostListener('window:mouseup')
    onMouseUp() {
        if (!this.isSelecting) return;
        this.isSelecting = false;
        this.validateSelection();
        this.startCell = null;
        this.clearTemporarySelection();
    }
    onTouchStart(event: TouchEvent) {
        event.preventDefault(); // Evita que la pantalla haga scroll
        if (event.touches.length > 0) {
            const touch = event.touches[0];
            const target = document.elementFromPoint(touch.clientX, touch.clientY);
            this.handleInputStart(target);
        }
    }

    onTouchMove(event: TouchEvent) {
        event.preventDefault(); // Vital para que no se mueva la página
        if (event.touches.length > 0) {
            const touch = event.touches[0];
            // Magia: Busca qué elemento HTML está bajo el dedo
            const target = document.elementFromPoint(touch.clientX, touch.clientY);
            this.handleInputMove(target);
        }
    }

    onTouchEnd() {
        this.onMouseUp(); // Reutilizamos la lógica de soltar del mouse
    }

    // Extrae fila/columna del elemento HTML y llama a la lógica
    private handleInputStart(target: Element | null) {
        if (target && target.hasAttribute('data-row')) {
            const r = Number(target.getAttribute('data-row'));
            const c = Number(target.getAttribute('data-col'));
            this.onMouseDown(r, c); // Llama a tu lógica existente
        }
    }

    private handleInputMove(target: Element | null) {
        // BUCLE DE SEGURIDAD: Si toco el texto <span>, sube al padre <div>
    while (target && !target.hasAttribute('data-row')) {
      target = target.parentElement;
    }
        
        if (target && target.hasAttribute('data-row')) {
            const r = Number(target.getAttribute('data-row'));
            const c = Number(target.getAttribute('data-col'));

            // Solo actualizamos si nos hemos movido a una celda diferente
            // para no saturar el cálculo
            this.onMouseEnter(r, c); // Llama a tu lógica existente
        }
    }
    // --- LÓGICA MATEMÁTICA DE SELECCIÓN ---

    private updateSelection(endR: number, endC: number) {
        if (!this.startCell) return;

        const startR = this.startCell.r;
        const startC = this.startCell.c;

        // Limpiamos selección visual previa
        this.clearTemporarySelection();

        // Calculamos si es una línea válida (H, V, D)
        // Delta (diferencia)
        const dr = endR - startR;
        const dc = endC - startC;

        // Es válida si:
        // 1. Fila igual (Horizontal) -> dr == 0
        // 2. Columna igual (Vertical) -> dc == 0
        // 3. Diagonal -> abs(dr) == abs(dc)

        if (dr === 0 || dc === 0 || Math.abs(dr) === Math.abs(dc)) {

            // Calculamos los pasos para recorrer la línea
            const stepR = dr === 0 ? 0 : (dr > 0 ? 1 : -1);
            const stepC = dc === 0 ? 0 : (dc > 0 ? 1 : -1);

            let currentR = startR;
            let currentC = startC;

            // Recorremos desde Inicio hasta Fin
            // Usamos un loop seguro (max grid size)
            const steps = Math.max(Math.abs(dr), Math.abs(dc));

            for (let i = 0; i <= steps; i++) {
                const cellKey = `${currentR}-${currentC}`;
                this.currentSelection.add(cellKey); // Añadir al Set visual

                // Avanzar
                currentR += stepR;
                currentC += stepC;
            }
        }
    }

    private clearTemporarySelection() {
        this.currentSelection.clear();
    }

    // --- VALIDACIÓN CON BACKEND ---

    private validateSelection() {
        if (this.currentSelection.size === 0) return;

        // 1. Reconstruir palabra visualmente (igual que antes)
        const cells: Cell[] = [];
        this.grid.forEach(row => row.forEach(cell => {
            if (this.currentSelection.has(`${cell.row}-${cell.col}`)) {
                cells.push(cell);
            }
        }));
        cells.sort((a, b) => (a.row - b.row) || (a.col - b.col));
        const wordForward = cells.map(c => c.char).join("");
        const wordReverse = wordForward.split('').reverse().join('');

        // 2. ¡VALIDACIÓN EN MEMORIA! (Sin HTTP)
        // Comprobamos si la palabra (o su inversa) está en la lista objetivo
        const targetWord = this.game?.wordsToFind.find(w =>
            w === wordForward || w === wordReverse
        );

        if (targetWord) {
            // ¡ACIERTO!
            // 3. Marcar visualmente (Usamos los IDs que ya tenemos en memoria)
            const selectedIds = Array.from(this.currentSelection);
            this.markAsFoundLocal(selectedIds, targetWord);
        } else {
            // FALLO (Opcional: Feedback visual rojo o sonido de error)
            console.log("Palabra incorrecta");
        }

        // Limpiamos selección visual
        this.clearTemporarySelection();
    }

    private markAsFoundLocal(cellIds: string[], word: string) {
        // 1. Evitar duplicados (si el usuario selecciona la misma palabra de nuevo)
        if (this.game?.foundWords.includes(word)) return;

        // 2. Actualizar Grid (Visual)
        cellIds.forEach(key => {
            const [r, c] = key.split('-').map(Number);
            this.grid[r][c].found = true;
        });

        // 3. Actualizar Estado Local
        this.game?.foundWords.push(word);

        // 4. ¿JUEGO TERMINADO?
        if (this.game?.foundWords.length === this.game?.wordsToFind.length) {
            this.handleGameWin();
        }
    }
    private handleGameOver(message: string) {
        this.timerSubscription?.unsubscribe();
        this.isGameOver = true;
        this.gameMessage = message;
        // Desactivar interacción si quieres
    }
    private handleGameWin() {
        this.handleGameOver("¡GENIAL, GANASTE! 🎉"); // Detiene timer

        this.wsService.saveGame(
            this.game!.foundWords,
            this.game!.timeElapsedSeconds || 0,
            true
        ).subscribe({
            next: () => console.log("Juego guardado")
        });
    }

    // Método para el botón "Atrás" (Guardar y Salir)
    saveAndQuit() {
        // Si el juego YA terminó (isGameOver), no intentes guardar de nuevo
      if (this.isGameOver) {
          this.router.navigate(['/wordsearch-lobby']);
          return;
      }

      // Si no ha terminado, guarda y sal
      this.wsService.saveGame(
          this.game!.foundWords,
          this.game!.timeElapsedSeconds || 0,
          false
      ).subscribe(() => {
          this.router.navigate(['/wordsearch-lobby']);
      });
    }

    // Helper para el HTML
    isCellSelected(r: number, c: number): boolean {
        return this.currentSelection.has(`${r}-${c}`);
    }

    goBack() {
    // Preguntamos si quiere abandonar
    if (confirm("¿Seguro que quieres salir? Se contará como partida abandonada.")) {
        
        // 1. Avisamos al backend para que "mate" la partida actual
        this.wsService.failGame().subscribe({
            next: () => {
                console.log("Partida abandonada.");
                // 2. Navegamos fuera
                this.router.navigate(['/wordsearch-lobby']);
            },
            error: (err) => {
                console.error("Error al abandonar:", err);
                // Navegamos igual para no atrapar al usuario
                this.router.navigate(['/wordsearch-lobby']);
            }
        });
    }
}
}