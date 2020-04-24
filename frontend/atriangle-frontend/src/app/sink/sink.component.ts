import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ProjectService} from "../core/service/project.service";
import {LoadingService} from "../core/service/loading.service";
import {AlertService} from "../core/service/alert.service";
import {FileUpload, FileUploadType, Project} from "../core/models";

@Component({
  selector: 'app-sink',
  templateUrl: './sink.component.html',
  styleUrls: ['./sink.component.scss']
})
export class SinkComponent implements OnInit {
  selectedId: string;
  selectedShaclId: string;
  filteredShaclFiles: FileUpload[];
  filteredRdfFiles: FileUpload[];


  constructor(public dialogRef: MatDialogRef<SinkComponent>,
              private projectService: ProjectService,
              private loadingService: LoadingService,
              private alertService: AlertService,
              @Inject(MAT_DIALOG_DATA) public data: Project) { }

  ngOnInit(): void {
    this.filteredRdfFiles = this.data.fileEvents.filter(xlsFIle => xlsFIle.eventType.toString() === FileUploadType[FileUploadType.RDF_FILE]);
    this.filteredShaclFiles = this.data.fileEvents.filter(xlsFIle => xlsFIle.eventType.toString() === FileUploadType[FileUploadType.SHACL_FILE]);
  }

  onNoClick(): void {
    this.dialogRef.close();
  }

  sink() {
    this.loadingService.showSpinner();
    this.projectService.sink(this.data.id, this.selectedId, this.selectedShaclId).subscribe(data => {
      this.loadingService.hideSpinner();
      this.alertService.openSnackBar(`sink started asynchronously. check logs for completion.`);
      this.onNoClick();
    });
  }
}
