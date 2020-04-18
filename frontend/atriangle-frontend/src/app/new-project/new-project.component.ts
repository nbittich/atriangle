import {ChangeDetectorRef, Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, Validators} from "@angular/forms";
import {Project} from "../core/models";
import {ProjectService} from "../core/service/project.service";
import {Router} from "@angular/router";
import {AlertService} from "../core/service/alert.service";

@Component({
  selector: 'app-new-project',
  templateUrl: './new-project.component.html',
  styleUrls: ['./new-project.component.scss']
})
export class NewProjectComponent implements OnInit {
  @Input()
  project: Project;

  @Output()
  onFinish: EventEmitter<Project> = new EventEmitter<Project>();

  nameFormControl: FormControl;
  descriptionFormControl: FormControl;

  constructor(private projectService: ProjectService,
              private router: Router,
              private cdRef: ChangeDetectorRef,
              private alertService: AlertService) {
  }

  ngOnInit(): void {
    if (!this.project) {
      this.project = new Project();
    }
    this.nameFormControl = new FormControl({value: this.project.name, disabled: this.project.id}, [
      Validators.required,
      Validators.minLength(7)
    ]);
    this.descriptionFormControl = new FormControl(this.project.description, [
      Validators.maxLength(255)
    ]);
  }

  submit() {
    const name = this.nameFormControl.value;
    const description = this.descriptionFormControl.value;
    if (!this.project.id) {
      this.projectService.newProject(name, description).subscribe(updatedProject => {
        this.reset();
        this.alertService.openSnackBar("Project with name " + name + " added");
        this.router.navigate(['/projects', updatedProject.id]);
      });
    } else {
      this.projectService.updateProject(this.project.id, description).subscribe(updatedProject => {
        this.reset();
        this.alertService.openSnackBar("Project with name " + name + " updated");
        this.router.navigate(['/projects', updatedProject.id]);
      })
    }
  }

  private reset() {
    this.project = new Project();
    this.nameFormControl.reset();
    this.descriptionFormControl.reset();
  }
}
