import { TestBed } from '@angular/core/testing';

import { ArchiveSessionCancelService } from './archive-session-cancel';

describe('ArchiveSessionCancelService', () => {
  let service: ArchiveSessionCancelService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(ArchiveSessionCancelService);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
