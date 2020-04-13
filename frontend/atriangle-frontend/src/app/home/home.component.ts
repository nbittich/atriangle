import {Component, OnInit} from '@angular/core';
import {InfoService} from "../core/service/info.service";
import {BackendInfo} from "../core/models";
import {Observable} from "rxjs";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  buildInfo$: Observable<BackendInfo>;

  constructor(private infoService: InfoService) {
  }

  ngOnInit(): void {
    this.buildInfo$ = this.infoService.getBuildInfo();
  }

}
