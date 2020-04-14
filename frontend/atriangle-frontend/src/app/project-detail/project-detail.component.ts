import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {FileUpload, Project} from "../core/models";
import {ProjectService} from "../core/service/project.service";
import {MatTableDataSource} from "@angular/material/table";
import {FileService} from "../core/service/file.service";

@Component({
  selector: 'app-project-detail',
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.scss']
})
export class ProjectDetailComponent implements OnInit {
  id: string;
  project: Project;
  datasource: MatTableDataSource<FileUpload>;

  displayedColumns: string[] = [
    'contentType', 'eventType', 'originalFilename', 'creationDate', 'action'
  ];

  applyFilter(value: string) {
    this.datasource.filter = value.trim().toLowerCase();

  }

  constructor(private route: ActivatedRoute,
              private fileService: FileService,
              private projectService: ProjectService) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.id = params["id"];
      this.projectService.getProject(this.id).subscribe(data => {
        this.project = data;
        this.datasource = new MatTableDataSource<FileUpload>(this.project.fileEvents);
      });
    });
  }

  download(fileUpload: FileUpload) {
    this.fileService.downloadFile(this.id, fileUpload);
  }
}
