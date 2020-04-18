import {HttpErrorResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class ErrorService {
  constructor(private snackBar: MatSnackBar) {
  }

  openSnackBar(err: HttpErrorResponse): void {
    const codeString: string = err.status ? err.status.toString() : '';
    const errorMessage = err.error || err.message || err.statusText || 'An error occurred';
    this.snackBar
      .open(`${errorMessage} - ${codeString}`, 'Close')
      .onAction()
      .subscribe(() => {
        this.snackBar.dismiss();
      });
  }
}
