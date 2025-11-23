import { ComponentFixture, TestBed } from '@angular/core/testing';

import { VerifyLink } from './verify-link';

describe('VerifyLink', () => {
  let component: VerifyLink;
  let fixture: ComponentFixture<VerifyLink>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerifyLink]
    })
    .compileComponents();

    fixture = TestBed.createComponent(VerifyLink);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
