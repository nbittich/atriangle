import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {environment} from "../../../environments/environment";
import {FileUploadType, Project} from "../models";
import {LogEvent} from "../models/log.event";


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

  getProject(id: string): Observable<Project> {
    return this.http.get<Project>(`${environment.backendUrl}/api/project/by-id/${id}`).pipe(
      map((data: Project) => {
        return data;
      })
    );
  }

  upload(formData: FormData, uploadType: FileUploadType): Observable<Project> {
    const url = ProjectService.getUrlFromUploadType(uploadType);
    return this.http.post<Project>(url, formData, {});
  }

  newProject(projectName: string, description: string): Observable<Project> {
    let url = environment.backendUrl + '/api/project?name=' + projectName;
    if (description && description.length) {
      url = url + '&description=' + description;
    }
    return this.http.post<Project>(url, {}, {});
  }

  private static getUrlFromUploadType(uploadType: FileUploadType): string {
    switch (uploadType) {
      case FileUploadType.RDF_FILE:
        return environment.backendUrl + "/api/project/add-rdf-file";
      case FileUploadType.SHACL_FILE:
        return environment.backendUrl + "/api/project/add-shacl-file";
      case FileUploadType.SKOS_FILE:
        return environment.backendUrl + "/api/project/add-skos-file";
      case FileUploadType.PROJECT_FILE:
      case FileUploadType.RAW_FILE:
        return environment.backendUrl + "/api/project/add-raw-file";
      case FileUploadType.FREEMARKER_TEMPLATE_FILE:
        return environment.backendUrl + "/api/project/add-sparql-query-template";
      default:
        throw new Error("not a supported file");
    }
  }

  updateProject(projectId: string, description: string): Observable<Project> {
    let url = environment.backendUrl + '/api/project/' + projectId + "/update-description?description=" + description;
    return this.http.post<Project>(url, {}, {});
  }

  getLogs(projectId: string) :Observable<LogEvent[]> {
    let url = environment.backendUrl + '/api/project/' + projectId + "/logs";
    return this.http.get<LogEvent[]>(url, {});
  }
}
