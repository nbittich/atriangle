import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FileUploadType, Project} from "../core/models";
import {ProjectService} from "../core/service/project.service";
import {catchError} from "rxjs/operators";

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss']
})
export class UploadComponent implements OnInit {

  @Output()
  progression: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  onFinish: EventEmitter<Project> = new EventEmitter<Project>();

  @Input()
  uploadType: string;

  @Input()
  title: string = 'Upload';

  @Input()
  projectId: string;
  @ViewChild("fileUpload", {static: false}) fileUpload: ElementRef;
  files = [];

  constructor(private projectService: ProjectService) {
  }

  ngOnInit(): void {
  }

  uploadFile(file) {
    const formData = new FormData();
    formData.append('file', file.data);
    formData.append('projectId', this.projectId);
    this.projectService.upload(formData, FileUploadType[this.uploadType]).pipe(
      catchError((error: Error) => {
        this.files = [];
        this.fileUpload.nativeElement.value = '';
        throw error;
      })
    ).subscribe((data: Project) => {
      this.files = [];
      this.fileUpload.nativeElement.value = '';
      this.onFinish.emit(data);
    });
  }

  private uploadFiles() {
    this.fileUpload.nativeElement.value = '';
    this.files.forEach(file => {
      this.uploadFile(file);
    });
  }

  onClick() {
    const fileUpload = this.fileUpload.nativeElement;
    fileUpload.onchange = () => {
      for (let index = 0; index < fileUpload.files.length; index++) {
        const file = fileUpload.files[index];
        this.files.push({data: file, inProgress: false, progress: 0});
      }
      this.uploadFiles();
    };
    fileUpload.click();
  }
}
