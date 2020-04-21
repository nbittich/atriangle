import {Injectable} from '@angular/core';
import {DialogService} from "./dialog.service";
import {SkosConversionComponent} from "../../skos-conversion/skos-conversion.component";
import {MatDialogRef} from "@angular/material/dialog";
import {LoadingComponent} from "../../loading/loading.component";
import {Loading} from "../models";

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  dialogRef: MatDialogRef<LoadingComponent>;
  constructor(public dialogService: DialogService) {
  }

  showSpinner(): void {
  this.dialogRef = this.dialogService.openDialog(LoadingComponent, {loading:true} as Loading);
  }

  hideSpinner() {
    if(!this.dialogRef) {
      console.log('no loading existing');
    }
    this.dialogRef.close();
    return this.dialogRef = null;
  }
}
