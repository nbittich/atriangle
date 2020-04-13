import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {HomeComponent} from "./home/home.component";
import {ProjectHomepageComponent} from "./project-homepage/project-homepage.component";
import {AuthGuard, RoleGuard} from "./core/guards";
import {ProjectDetailComponent} from "./project-detail/project-detail.component";


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

  {
    path: 'projects/:id',
    component: ProjectDetailComponent,
    canActivate: [AuthGuard, RoleGuard],
    data: {expectedRole: ['ADMIN']}
  },
];

@NgModule({
  imports: [RouterModule.forRoot(routes, {useHash: true})],
  exports: [RouterModule]
})
export class AppRoutingModule {

}
