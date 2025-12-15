import { TestBed } from '@angular/core/testing';

import { ArchiveSessionGetService } from './archive-session-get';

describe('ArchiveSessionGetService', () => {
  let service: ArchiveSessionGetService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ArchiveSessionGetService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
