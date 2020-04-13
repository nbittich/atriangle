import {Component, OnInit} from '@angular/core';
import {Observable} from "rxjs";
import {BackendInfo} from "../core/models";
import {InfoService} from "../core/service/info.service";

@Component({
  selector: 'app-build-info',
  templateUrl: './build-info.component.html',
  styleUrls: ['./build-info.component.scss']
})
export class BuildInfoComponent implements OnInit {
  buildInfo$: Observable<BackendInfo>;

  constructor(private infoService: InfoService) {
  }

  ngOnInit(): void {
    this.buildInfo$ = this.infoService.getBuildInfo();
  }

}
