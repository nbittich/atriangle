import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { ActivationEnd, Router } from '@angular/router';
import { Observable } from 'rxjs';
import { takeUntil } from 'rxjs/operators';
import {HttpCancelRequestService} from "../service/http-cancel-request.service";


@Injectable()
export class CancelHttpInterceptor implements HttpInterceptor {
  constructor(router: Router, private httpCancelRequestService: HttpCancelRequestService) {
    router.events.subscribe(event => {
      if (event instanceof ActivationEnd) {
        this.httpCancelRequestService.cancelPendingRequests();
      }
    });
  }

  intercept<T>(req: HttpRequest<T>, next: HttpHandler): Observable<HttpEvent<T>> {
    return next.handle(req).pipe(takeUntil(this.httpCancelRequestService.onCancelPendingRequests()));
  }
}
