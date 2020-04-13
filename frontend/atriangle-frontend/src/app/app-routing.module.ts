import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {ProjectHomepageComponent} from "./project-homepage/project-homepage.component";
import {AuthGuard, RoleGuard} from "./core/guards";


const routes: Routes = [
  {
    path: '',
    component: HomeComponent
  },
  {
    path: 'projects',
    component: ProjectHomepageComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {expectedRole: ['ADMIN']}
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule {

}
