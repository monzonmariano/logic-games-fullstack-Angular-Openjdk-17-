import { ComponentFixture, TestBed } from '@angular/core/testing';

import { Numpad } from './numpad';

describe('Numpad', () => {
  let component: Numpad;
  let fixture: ComponentFixture<Numpad>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [Numpad]
    })
    .compileComponents();

    fixture = TestBed.createComponent(Numpad);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
