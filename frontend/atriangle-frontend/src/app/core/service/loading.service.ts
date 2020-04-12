import { Injectable } from '@angular/core';
import { Subject } from 'rxjs';

import { HttpCancelRequestService } from './http-cancel-request.service';

@Injectable({
  providedIn: 'root'
})
export class LoadingService  {
  loading: Subject<boolean>;
  private count: number = 0;

  constructor(private httpCancelRequestService: HttpCancelRequestService) {
    this.loading = new Subject<boolean>();
    this.httpCancelRequestService
      .onCancelPendingRequests()
      .subscribe(() => {
        // this.hideSpinner();
        // TODO test, temp fix (all canceled requests are not intercepted for now...
        this.count = 0;
        this.loading.next(false);
      });
  }

  showSpinner(): void {
    this.count++;
    if (this.count === 1) {
      setTimeout(() => {
        if (this.count > 0) {
          this.loading.next(true);
        }
      }, 800);
    }
  }

  hideSpinner(): void {
    this.count--;
    if (this.count < 1) {
      this.count = 0;
      this.loading.next(false);
    }
  }
}
