import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatButtonModule} from "@angular/material/button";
import {MatSortModule} from "@angular/material/sort";
import {MatCardModule} from "@angular/material/card";
import {MatInputModule} from "@angular/material/input";
import {MatSnackBarModule} from "@angular/material/snack-bar";
import {MatToolbarModule} from "@angular/material/toolbar";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatSelectModule} from "@angular/material/select";
import {MatIconModule} from "@angular/material/icon";
import {MatListModule} from "@angular/material/list";
import {MatSidenavModule} from "@angular/material/sidenav";
import {MatStepperModule} from "@angular/material/stepper";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatChipsModule} from "@angular/material/chips";
import {MatMenuModule} from "@angular/material/menu";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatTableModule} from "@angular/material/table";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatDialogModule} from "@angular/material/dialog";
import {HTTP_INTERCEPTORS, HttpClientModule} from "@angular/common/http";
import {FaIconLibrary, FontAwesomeModule} from "@fortawesome/angular-fontawesome";
import {AuthInterceptor, ErrorInterceptor, LoadingInterceptor} from "./core/interceptors";
import {FlexLayoutModule} from "@angular/flex-layout";
import {LoginComponent} from './login/login.component';
import {fas} from "@fortawesome/free-solid-svg-icons";
import {far} from "@fortawesome/free-regular-svg-icons";
import {DeviceDetectorModule} from "ngx-device-detector";
import {MenuComponent} from './menu/menu.component';
import {fab} from "@fortawesome/free-brands-svg-icons";
import {HomeComponent} from './home/home.component';
import {BuildInfoComponent} from './build-info/build-info.component';
import {ProjectHomepageComponent} from './project-homepage/project-homepage.component';
import {ProjectDetailComponent} from './project-detail/project-detail.component';
import {MatExpansionModule} from "@angular/material/expansion";
import {UploadComponent} from './upload/upload.component';
import {MatProgressBarModule} from "@angular/material/progress-bar";

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    MenuComponent,
    HomeComponent,
    BuildInfoComponent,
    ProjectHomepageComponent,
    ProjectDetailComponent,
    UploadComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    MatChipsModule,
    MatSidenavModule,
    MatToolbarModule,
    MatFormFieldModule,
    FontAwesomeModule,
    ReactiveFormsModule,
    MatMenuModule,
    MatCheckboxModule,
    BrowserAnimationsModule,
    MatListModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatInputModule,
    MatCardModule,
    MatButtonModule,
    MatSnackBarModule,
    MatIconModule,
    MatDialogModule,
    FormsModule,
    MatSelectModule,
    FlexLayoutModule,
    MatAutocompleteModule,
    MatStepperModule,
    DeviceDetectorModule.forRoot(),
    MatExpansionModule,
    MatProgressBarModule
  ],
  providers: [
    {provide: HTTP_INTERCEPTORS, useClass: LoadingInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: AuthInterceptor, multi: true},
    {provide: HTTP_INTERCEPTORS, useClass: ErrorInterceptor, multi: true}
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(library: FaIconLibrary) {
    library.addIconPacks(fas,far,fab);
  }
}
