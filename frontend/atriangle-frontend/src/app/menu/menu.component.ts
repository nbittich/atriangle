import {Component, OnInit} from '@angular/core';
import {Menu, MenuSection} from "../core/models";
import menuList from "./menu-list";

@Component({
  selector: 'app-menu',
  templateUrl: './menu.component.html',
  styleUrls: ['./menu.component.scss']
})
export class MenuComponent implements OnInit {

  menuApiList: Menu[];
  menuProxyList: Menu[];
  constructor() { }

  ngOnInit(): void {
    this.menuApiList = menuList.filter(menu => menu.section  === MenuSection.REST_ENDPOINT) || [];
    this.menuProxyList = menuList.filter(menu => menu.section  === MenuSection.PROXY_ENDPOINT) || [];
  }

}
