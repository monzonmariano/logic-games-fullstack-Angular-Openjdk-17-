import { TestBed } from '@angular/core/testing';

import { GameState } from './game-state';

describe('GameState', () => {
  let service: GameState;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(GameState);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
