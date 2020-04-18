export interface Menu {
  url: string;
  name: string;
  icon: MenuIcon;
  section: MenuSection;

}

export interface MenuIcon {
  pack: string;
  name: string;
}


export enum MenuSection {
  REST_ENDPOINT, PROXY_ENDPOINT, FRONTEND_ENDPOINT
}
