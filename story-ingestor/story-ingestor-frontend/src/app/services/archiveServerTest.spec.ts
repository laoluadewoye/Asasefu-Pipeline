import { TestBed } from '@angular/core/testing';

import { ArchiveServerTestService } from './archiveServerTest';

describe('ArchiveServerTestService', () => {
  let service: ArchiveServerTestService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ArchiveServerTestService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
