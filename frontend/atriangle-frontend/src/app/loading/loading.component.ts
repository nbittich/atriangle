import {Component, Inject, OnInit} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Loading, Project} from "../core/models";

@Component({
  selector: 'app-loading',
  templateUrl: './loading.component.html',
  styleUrls: ['./loading.component.scss']
})
export class LoadingComponent implements OnInit {

  constructor(
    @Inject(MAT_DIALOG_DATA) public data: Loading,
    public dialogRef: MatDialogRef<LoadingComponent>) { }

  ngOnInit(): void {
  }

}
