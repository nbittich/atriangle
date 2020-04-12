import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import {AuthService} from "../service/auth.service";
import {ErrorService} from "../service/error.service";


@Injectable({
  providedIn: 'root'
})
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private authenticationService: AuthService, private router: Router, private errorService: ErrorService) {}

  intercept<T>(request: HttpRequest<T>, next: HttpHandler): Observable<HttpEvent<T>> {
    return next.handle(request).pipe(
      catchError((err: HttpErrorResponse) => {
        if (err.status === 401 || err.status === 403) {
          this.authenticationService.logout();
          this.router.navigate(['']);
        } else if (err.status !== 404) {
          // in user-profile and user-creation, invalid data use 404 to be handled internally within components
          this.errorService.openSnackBar(err);
        }
        return throwError(err);
      })
    );
  }
}
