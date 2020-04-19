import {Component, Inject, OnInit} from '@angular/core';
import {FileUpload, FileUploadType, Project} from "../core/models";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

@Component({
  selector: 'app-skos-conversion',
  templateUrl: './skos-conversion.component.html',
  styleUrls: ['./skos-conversion.component.scss']
})
export class SkosConversionComponent implements OnInit {
  filteredXlsFiles: FileUpload[];
  selectedId: string;

  constructor(public dialogRef: MatDialogRef<SkosConversionComponent>,
              @Inject(MAT_DIALOG_DATA) public data: Project) {
  }

  ngOnInit(): void {
    this.filteredXlsFiles = this.data.fileEvents.filter(xlsFIle => xlsFIle.eventType.toString() === FileUploadType[FileUploadType.SKOS_FILE]);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  convert() {
    console.log(this.selectedId);
  }
}
