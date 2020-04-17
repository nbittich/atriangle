import {Component, ElementRef, EventEmitter, Input, OnInit, Output, ViewChild} from '@angular/core';
import {FileUploadType} from "../core/models";
import {ProjectService} from "../core/service/project.service";
import {HttpEventType} from "@angular/common/http";
import {map} from "rxjs/operators";

@Component({
  selector: 'app-upload',
  templateUrl: './upload.component.html',
  styleUrls: ['./upload.component.scss']
})
export class UploadComponent implements OnInit {

  @Output()
  progression: EventEmitter<number> = new EventEmitter<number>();

  @Output()
  onFinish: EventEmitter<string> = new EventEmitter<string>();

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
    file.inProgress = true;
    this.projectService.upload(formData, FileUploadType[this.uploadType]).pipe(
      map(event => {
        switch (event.type) {
          case HttpEventType.UploadProgress:
            let progress = Math.round(event.loaded * 100 / event.total);
            this.progression.emit(progress);
            file.progress = progress;
            break;
          case HttpEventType.Response:
            return event;
        }
      }),
      /*catchError((error: HttpErrorResponse) => {
        file.inProgress = false;
        return of(`${file.data.name} upload failed.`);
      })*/
    ).subscribe((event: any) => {
      this.files = [];
      this.fileUpload.nativeElement.value = '';
      this.onFinish.emit('done');
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
