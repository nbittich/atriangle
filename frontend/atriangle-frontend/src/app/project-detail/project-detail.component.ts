import {ChangeDetectorRef, Component, OnInit, ViewChild} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {FileUpload, Project} from "../core/models";
import {ProjectService} from "../core/service/project.service";
import {MatTableDataSource} from "@angular/material/table";
import {FileService} from "../core/service/file.service";
import {MatPaginator} from "@angular/material/paginator";
import {AlertService} from "../core/service/alert.service";
import {LogsComponent} from "../logs/logs.component";
import {DialogService} from "../core/service/dialog.service";
import {SkosConversionComponent} from "../skos-conversion/skos-conversion.component";
import {SinkComponent} from "../sink/sink.component";
import {timer} from "rxjs";
import {delay} from "rxjs/operators";

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
              private alertService: AlertService,
              private projectService: ProjectService,
              private dialogService: DialogService,
              private cdr: ChangeDetectorRef) {
  }

  openLogs(): void {
    this.dialogService.openDialog(LogsComponent, this.project);
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

  openSkosModal() {
    const dialogRef = this.dialogService.openDialog(SkosConversionComponent, this.project);
    dialogRef.afterClosed().subscribe(result => {
      this.getProject();
    });
  }

  openSinkModal() {
    const dialogRef = this.dialogService.openDialog(SinkComponent, this.project);
    dialogRef.afterClosed().pipe(delay(5000)).subscribe(result =>{
      this.getProject();
    });
  }
}
