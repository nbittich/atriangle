import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';

@Injectable({
  providedIn: 'root'
})
export class AlertService {
  constructor(private snackBar: MatSnackBar) {
  }

  openSnackBar(message: string): void {
    this.snackBar
      .open(`${message}`, 'Close', {
        duration: 3000
      })
      .onAction()
      .subscribe(() => {
        this.snackBar.dismiss();
      });
  }
}
