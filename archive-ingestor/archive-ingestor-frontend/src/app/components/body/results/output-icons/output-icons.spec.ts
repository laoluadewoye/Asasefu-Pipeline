import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OutputIcons } from './output-icons';

describe('OutputIcons', () => {
  let component: OutputIcons;
  let fixture: ComponentFixture<OutputIcons>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OutputIcons]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OutputIcons);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
