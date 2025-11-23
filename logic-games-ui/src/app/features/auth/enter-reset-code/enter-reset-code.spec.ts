import { ComponentFixture, TestBed } from '@angular/core/testing';

import { EnterResetCode } from './enter-reset-code';

describe('EnterResetCode', () => {
  let component: EnterResetCode;
  let fixture: ComponentFixture<EnterResetCode>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [EnterResetCode]
    })
    .compileComponents();

    fixture = TestBed.createComponent(EnterResetCode);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
