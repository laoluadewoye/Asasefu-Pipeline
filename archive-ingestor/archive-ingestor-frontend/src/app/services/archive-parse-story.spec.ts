import { TestBed } from '@angular/core/testing';

import { ArchiveParseStoryService } from './archive-parse-story';

describe('ArchiveParseStoryService', () => {
  let service: ArchiveParseStoryService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ArchiveParseStoryService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
