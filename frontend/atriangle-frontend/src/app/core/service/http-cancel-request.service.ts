import { Injectable } from '@angular/core';
import { Observable, Subject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class HttpCancelRequestService {
  private pendingHTTPRequests$: Subject<void> = new Subject<void>();

  cancelPendingRequests(): void {
    this.pendingHTTPRequests$.next();
  }

  onCancelPendingRequests(): Observable<void> {
    return this.pendingHTTPRequests$.asObservable();
  }
}
