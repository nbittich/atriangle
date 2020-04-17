import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {environment} from "../../../environments/environment";
import {FileUploadType, Project} from "../models";


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

  upload(formData: FormData, projectId: string, uploadType: FileUploadType) {
    const url = this.getUrlFromUploadType(uploadType);
    return this.http.post<any>(url, formData, {
      reportProgress: true,
      observe: 'events'
    });
  }

  private getUrlFromUploadType(uploadType:FileUploadType) : string{
    switch (uploadType) {
      case FileUploadType.RDF_FILE: return environment.backendUrl + "/project/add-rdf-file";
      case FileUploadType.SHACL_FILE: return environment.backendUrl + "/project/add-shacl-file";
      case FileUploadType.PROJECT_FILE:
      case FileUploadType.RAW_FILE: return environment.backendUrl + "/project/add-raw-file";
      case FileUploadType.FREEMARKER_TEMPLATE_FILE: return environment.backendUrl + "/project/add-sparql-query-template";
      default: throw new Error("not a supported file");
    }
  }
}
