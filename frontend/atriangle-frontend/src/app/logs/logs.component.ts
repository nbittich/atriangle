import {ChangeDetectorRef, Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {LogEvent} from "../core/models/log.event";
import {ProjectService} from "../core/service/project.service";
import {MatPaginator} from "@angular/material/paginator";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Project} from "../core/models";
import {LoadingService} from "../core/service/loading.service";


@Component({
  selector: 'app-logs',
  templateUrl: './logs.component.html',
  styleUrls: ['./logs.component.scss']
})
export class LogsComponent implements OnInit {
  datasource: MatTableDataSource<LogEvent>;

  displayedColumns: string[] = [
    'creationDate',
    'message',
    'type'
  ];

  @ViewChild(MatPaginator) paginator: MatPaginator;
  loaded: boolean;

  constructor(private projectService: ProjectService,
              public dialogRef: MatDialogRef<LogsComponent>,
              private loadingService: LoadingService,
              @Inject(MAT_DIALOG_DATA) public data: Project,
              private cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.loadingService.showSpinner();
    this.projectService.getLogs(this.data.id).subscribe(data => {
      this.datasource = new MatTableDataSource<LogEvent>(data);
      this.cdr.detectChanges();
      this.datasource.paginator = this.paginator;
      this.loadingService.hideSpinner();
      this.loaded = true;
    });
  }


  onNoClick(): void {
    this.dialogRef.close();
  }

  applyFilter(value: string) {
    this.datasource.filter = value.trim().toLowerCase();

  }

}
