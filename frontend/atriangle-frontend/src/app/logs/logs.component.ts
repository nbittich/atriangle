import {ChangeDetectorRef, Component, Inject, OnInit, ViewChild} from '@angular/core';
import {MatTableDataSource} from "@angular/material/table";
import {LogEvent} from "../core/models/log.event";
import {ProjectService} from "../core/service/project.service";
import {MatPaginator} from "@angular/material/paginator";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";

export interface DialogData {
  id: string;
}
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

  constructor(private projectService:ProjectService,
              public dialogRef: MatDialogRef<LogsComponent>,
              @Inject(MAT_DIALOG_DATA) public data: DialogData,
              private cdr: ChangeDetectorRef) { }

  ngOnInit(): void {
    this.projectService.getLogs(this.data.id).subscribe(data => {
      this.datasource = new MatTableDataSource<LogEvent>(data);
      this.cdr.detectChanges();
      this.datasource.paginator = this.paginator;
    });
  }


  onNoClick(): void {
    this.dialogRef.close();
  }

  applyFilter(value: string) {
    this.datasource.filter = value.trim().toLowerCase();

  }

}
