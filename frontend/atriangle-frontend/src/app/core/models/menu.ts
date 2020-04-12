export class Menu {
  url: string;
  name: string;
  icon:MenuIcon;
  section: MenuSection;

}

export class MenuIcon {
  pack: string;
  name: string;
}


export enum MenuSection {
  REST_ENDPOINT, PROXY_ENDPOINT
}
