import { TestBed } from '@angular/core/testing';

import { ArchiveServerSpecService } from './archive-server-spec';

describe('ArchiveServerSpecService', () => {
  let service: ArchiveServerSpecService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ArchiveServerSpecService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
