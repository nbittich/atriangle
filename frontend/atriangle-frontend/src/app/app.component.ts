import {Component, OnInit} from '@angular/core';
import {AuthService} from "./core/service/auth.service";
import {MatDialog} from "@angular/material/dialog";
import {Router} from "@angular/router";
import {User} from "./core/models/user";
import {DeviceDetectorService} from "ngx-device-detector";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit{
  opened: boolean;
  showLoginForm: boolean;
  projects: number= 0;
  events: string[] = [];

  constructor(private authService: AuthService,
              public dialog: MatDialog,
              private deviceService: DeviceDetectorService,
              private router: Router) {
  }


  getUserInfo(): User | undefined {
    return this.isLoggedIn() ? this.authService.getUser() : undefined;
  }

  logout() {
    this.authService.logout();
    this.showLoginForm = false;
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  isAdmin() {
    return this.authService.hasRole(["ADMIN"]);
  }

  ngOnInit(): void {
    this.opened = this.bigScreen();
  }

  bigScreen(): boolean {
    return !this.deviceService.isMobile() && !this.deviceService.isTablet()
  }
}
