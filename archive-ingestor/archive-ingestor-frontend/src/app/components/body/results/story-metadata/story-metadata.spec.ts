import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StoryMetadata } from './story-metadata';

describe('StoryMetadata', () => {
  let component: StoryMetadata;
  let fixture: ComponentFixture<StoryMetadata>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [StoryMetadata]
    })
    .compileComponents();

    fixture = TestBed.createComponent(StoryMetadata);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
