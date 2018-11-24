import {Injectable} from '@angular/core';
import {HttpEvent, HttpInterceptor, HttpHandler, HttpRequest} from '@angular/common/http';

import {Observable} from 'rxjs';
import {finalize} from 'rxjs/internal/operators';
import {RequestPendingService} from './request-pending.service';

@Injectable()
export class PendingRequestInterceptor implements HttpInterceptor {

  constructor(private readonly requestPendingService: RequestPendingService) {}

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    this.requestPendingService.putRequest();
    return next.handle(req).pipe(finalize(() => this.requestPendingService.popRequest()));
  }
}
