import {Injectable} from '@angular/core';
import {Subject} from 'rxjs';

@Injectable()
export class RequestPendingService {

  public readonly requestPendingSubject: Subject<boolean> = new Subject<boolean>();
  private numberOfPendingRequests = 0;

  constructor() { }

  putRequest() {
    this.numberOfPendingRequests++;
    this.requestPendingSubject.next(this.isRequestPending());
  }

  popRequest() {
    this.numberOfPendingRequests--;
    this.requestPendingSubject.next(this.isRequestPending());
  }

  private isRequestPending() {
    return this.numberOfPendingRequests !== 0;
  }
}
