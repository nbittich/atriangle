import {Component, Inject, OnInit} from '@angular/core';
import {FileUpload, FileUploadType, Project} from "../core/models";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ProjectService} from "../core/service/project.service";
import {AlertService} from "../core/service/alert.service";
import {LoadingService} from "../core/service/loading.service";

@Component({
  selector: 'app-skos-conversion',
  templateUrl: './skos-conversion.component.html',
  styleUrls: ['./skos-conversion.component.scss']
})
export class SkosConversionComponent implements OnInit {
  filteredXlsFiles: FileUpload[];
  selectedId: string;

  constructor(public dialogRef: MatDialogRef<SkosConversionComponent>,
              private projectService: ProjectService,
              private loadingService: LoadingService,
              private alertService: AlertService,
              @Inject(MAT_DIALOG_DATA) public data: Project) {
  }

  ngOnInit(): void {
    this.filteredXlsFiles = this.data.fileEvents.filter(xlsFIle => xlsFIle.eventType.toString() === FileUploadType[FileUploadType.SKOS_FILE]);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  convert() {
    this.loadingService.showSpinner();
    this.projectService.skosConversion(this.data.id, this.selectedId).subscribe(project => {
      this.loadingService.hideSpinner();
      this.alertService.openSnackBar(`skos conversion completed`);
      this.onNoClick();
    });
  }
}
