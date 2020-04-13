import {Component, OnInit, ViewChild} from '@angular/core';
import {ProjectService} from "../core/service/project.service";
import {Project} from "../core/models";
import {MatTableDataSource} from "@angular/material/table";
import {MatPaginator} from "@angular/material/paginator";

@Component({
  selector: 'app-project-homepage',
  templateUrl: './project-homepage.component.html',
  styleUrls: ['./project-homepage.component.scss']
})
export class ProjectHomepageComponent implements OnInit {
  datasource: MatTableDataSource<Project>;

  displayedColumns: string[] = ['id',
    'name', 'files', 'action'
  ];

  constructor(private projectService: ProjectService) {
  }

  private paginator: MatPaginator;

  @ViewChild(MatPaginator) set matPaginator(mp: MatPaginator) {
    this.paginator = mp;
    this.datasource.paginator = this.paginator;
  }

  ngOnInit(): void {
    this.projectService.getProjects().subscribe(data => {
      this.datasource = new MatTableDataSource<Project>(data);
    });
  }


  applyFilter(value: string) {
    this.datasource.filter = value.trim().toLowerCase();

  }

}
