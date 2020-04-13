import {Component, OnInit} from '@angular/core';
import {Menu, MenuSection} from "../core/models";
import menuList from "./menu-list";
import {AuthService} from "../core/service/auth.service";

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent implements OnInit {

  menuApiList: Menu[];
  menuProxyList: Menu[];
  menuProxyFrontend: Menu[];

  constructor(private authService: AuthService) {
  }

  ngOnInit(): void {
    this.menuApiList = menuList.filter(menu => menu.section === MenuSection.REST_ENDPOINT) || [];
    this.menuProxyList = menuList.filter(menu => menu.section === MenuSection.PROXY_ENDPOINT) || [];
    this.menuProxyFrontend = menuList.filter(menu => menu.section === MenuSection.FRONTEND_ENDPOINT) || [];
  }

  isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  isAdmin() {
    return this.authService.hasRole(["ADMIN"]);
  }

}
