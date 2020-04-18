import {ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {FileUpload, Project} from "../core/models";
import {ProjectService} from "../core/service/project.service";
import {MatTableDataSource} from "@angular/material/table";
import {FileService} from "../core/service/file.service";
import {MatPaginator} from "@angular/material/paginator";
import {AlertService} from "../core/service/alert.service";
import {MatDialog} from "@angular/material/dialog";
import {LogsComponent} from "../logs/logs.component";

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
  @ViewChild(MatPaginator) paginator: MatPaginator;

  applyFilter(value: string) {
    this.datasource.filter = value.trim().toLowerCase();

  }

  constructor(private route: ActivatedRoute,
              private fileService: FileService,
              public dialog: MatDialog,
              private alertService: AlertService,
              private projectService: ProjectService,
              private cdr: ChangeDetectorRef) {
  }
  openLogs(): void {
    const dialogRef = this.dialog.open(LogsComponent, {
      //width: '100vw',
      //maxWidth: '100vw',
      data: {id:this.project.id}
    });

    dialogRef.afterClosed().subscribe(result => {
      console.debug('The dialog for logs was closed', result);
    });
  }


  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.id = params["id"];
      this.getProject();
    });
  }

  getProject(): void {
    this.projectService.getProject(this.id).subscribe(data => {
      this.project = data;
      this.reloadDataTable();
    });
  }

  private reloadDataTable() {
    this.datasource = new MatTableDataSource<FileUpload>(this.project.fileEvents);
    this.cdr.detectChanges();
    this.datasource.paginator = this.paginator;
  }

  download(fileUpload: FileUpload) {
    this.fileService.downloadFile(this.id, fileUpload);
  }

  onFinishUpload($event: Project) {
    this.project = $event;
    this.reloadDataTable();
    this.alertService.openSnackBar('file added');
  }

}
