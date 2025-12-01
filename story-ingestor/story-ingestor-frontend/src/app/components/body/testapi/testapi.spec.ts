import { ComponentFixture, TestBed } from '@angular/core/testing';

import { TestAPI } from './testapi';

describe('Testapi', () => {
  let component: TestAPI;
  let fixture: ComponentFixture<TestAPI>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TestAPI]
    })
    .compileComponents();

    fixture = TestBed.createComponent(TestAPI);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
