import { TestBed } from '@angular/core/testing';

import { Testapi } from './testapi';

describe('Testapi', () => {
  let service: Testapi;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(Testapi);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
