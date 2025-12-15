import { TestBed } from '@angular/core/testing';

import { ArchiveParseChapterService } from './archive-parse-chapter';

describe('ArchiveParseChapterService', () => {
  let service: ArchiveParseChapterService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ArchiveParseChapterService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
