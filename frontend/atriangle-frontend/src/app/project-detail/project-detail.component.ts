import {Component, OnInit} from '@angular/core';
import {ActivatedRoute} from "@angular/router";
import {Project} from "../core/models";
import {ProjectService} from "../core/service/project.service";

@Component({
  selector: 'app-project-detail',
  templateUrl: './project-detail.component.html',
  styleUrls: ['./project-detail.component.scss']
})
export class ProjectDetailComponent implements OnInit {
  id: string;
  project: Project;

  constructor(private route: ActivatedRoute, private projectService: ProjectService) {
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.id = params["id"];
      this.projectService.getProject(this.id).subscribe(data => this.project = data);
    });
  }

}
