import {TestBed} from '@angular/core/testing';
import {RequestPendingService} from './request-pending.service';

describe('RequestPendingService', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      RequestPendingService
    ]
  }));

  it('should be created', () => {
    const service: RequestPendingService = TestBed.get(RequestPendingService);
    expect(service).toBeTruthy();
  });
});
