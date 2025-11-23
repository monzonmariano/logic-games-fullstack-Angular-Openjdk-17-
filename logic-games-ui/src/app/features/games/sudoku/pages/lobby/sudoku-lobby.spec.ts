import { ComponentFixture, TestBed } from '@angular/core/testing';

import { SudokuLobby } from './sudoku-lobby';

describe('SudokuLobby', () => {
  let component: SudokuLobby;
  let fixture: ComponentFixture<SudokuLobby>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [SudokuLobby]
    })
    .compileComponents();

    fixture = TestBed.createComponent(SudokuLobby);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
