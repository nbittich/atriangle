import {Injectable} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ComponentType} from "@angular/cdk/overlay";

@Injectable({
  providedIn: 'root'
})
export class DialogService {
  constructor(public dialog: MatDialog) {
  }

  openDialog(component: ComponentType<any>, data: any): MatDialogRef<any> {
    const dialogRef = this.dialog.open(component, {
      data: data
    });

    dialogRef.afterClosed().subscribe(result => {
      console.debug('The dialog was closed', result);
    });

    return dialogRef;

  }
}
