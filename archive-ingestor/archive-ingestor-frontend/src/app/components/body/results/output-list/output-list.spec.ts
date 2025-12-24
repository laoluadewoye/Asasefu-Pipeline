import { ComponentFixture, TestBed } from '@angular/core/testing';

import { OutputList } from './output-list';

describe('OutputList', () => {
  let component: OutputList;
  let fixture: ComponentFixture<OutputList>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [OutputList]
    })
    .compileComponents();

    fixture = TestBed.createComponent(OutputList);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
