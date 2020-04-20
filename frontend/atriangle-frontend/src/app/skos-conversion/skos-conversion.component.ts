import {Component, Inject, OnInit} from '@angular/core';
import {FileUpload, FileUploadType, Project} from "../core/models";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ProjectService} from "../core/service/project.service";
import {AlertService} from "../core/service/alert.service";

@Component({
  selector: 'app-skos-conversion',
  templateUrl: './skos-conversion.component.html',
  styleUrls: ['./skos-conversion.component.scss']
})
export class SkosConversionComponent implements OnInit {
  filteredXlsFiles: FileUpload[];
  selectedId: string;
  loading: boolean;

  constructor(public dialogRef: MatDialogRef<SkosConversionComponent>,
              private projectService: ProjectService,
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
    this.loading = true;
    this.projectService.skosConversion(this.data.id, this.selectedId).subscribe(project => {
      this.loading = false;
      this.alertService.openSnackBar(`skos conversion completed`);
      this.onNoClick();
    });
  }
}
