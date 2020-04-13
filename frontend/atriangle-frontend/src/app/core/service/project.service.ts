import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {environment} from "../../../environments/environment";
import {Project} from "../models";


@Injectable({
  providedIn: 'root'
})
export class ProjectService {

  constructor(private http: HttpClient) {
  }

  public getProjects(): Observable<Project[]> {
    return this.http.get<Project[]>(`${environment.backendUrl}/api/project/list`).pipe(
      map((data: Project[]) => {
        return data;
      })
    );
  }
}
