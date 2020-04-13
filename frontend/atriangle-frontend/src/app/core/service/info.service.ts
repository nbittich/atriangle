import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs';
import {map} from 'rxjs/operators';
import {environment} from "../../../environments/environment";
import {BackendInfo} from "../models/backend.info";


@Injectable({
  providedIn: 'root'
})
export class InfoService {

  constructor(private http: HttpClient) {
  }

  async init(): Promise<unknown> {
    return this.getBuildInfo().toPromise();
  }

  public getBuildInfo(): Observable<BackendInfo> {
    return this.http.get<BackendInfo>(`${environment.backendUrl}/actuator/info`).pipe(
      map((data: any) => {
        return data.build;
      })
    );
  }
}
