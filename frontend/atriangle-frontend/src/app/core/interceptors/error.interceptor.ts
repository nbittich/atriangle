import {HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Router} from '@angular/router';
import {Observable, throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {AuthService} from "../service/auth.service";
import {AlertService} from "../service/alert.service";
import {DialogService} from "../service/dialog.service";


@Injectable({
  providedIn: 'root'
})
export class ErrorInterceptor implements HttpInterceptor {
  constructor(private authenticationService: AuthService,
              private router: Router,
              private dialogService: DialogService,
              private alertService: AlertService) {
  }

  intercept<T>(request: HttpRequest<T>, next: HttpHandler): Observable<HttpEvent<T>> {
    return next.handle(request).pipe(
      catchError((err: HttpErrorResponse) => {
        this.dialogService.closeAll();
        if (err.status === 401 || err.status === 403) {
          this.authenticationService.logout();
          this.router.navigate(['']);
        } else if (err.status !== 404) {
          const codeString: string = err.status ? err.status.toString() : '';
          const errorMessage = err.error || err.message || err.statusText || 'An error occurred';
          this.alertService.openSnackBar(codeString + ' - ' + errorMessage);
        }
        return throwError(err);
      })
    );
  }
}
