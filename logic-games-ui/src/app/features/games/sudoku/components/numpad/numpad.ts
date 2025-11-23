import { Component , EventEmitter, Output} from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button'; 
import { MatIconModule } from '@angular/material/icon';  


@Component({
  selector: 'app-numpad',
  imports: [
    CommonModule,
    MatButtonModule, 
    MatIconModule    
  ],
  templateUrl: './numpad.html',
  styleUrl: './numpad.scss',
})
export class Numpad {

  // 1. Crea un "emisor de eventos"
  // Emitirá un número (1-9) o 'null' (para borrar)
  @Output() numpadClick = new EventEmitter<number | null>();

  constructor() { }

  // 2. Este método se llama desde el HTML
  onNumberClick(value: number | null): void {
    // 3. ¡Emite el valor hacia el componente "padre"!
    this.numpadClick.emit(value);
  }

}
