import {HttpClient} from '@angular/common/http';
import {Injectable} from '@angular/core';
import {environment} from "../../../environments/environment";
import {FileUpload} from "../models";


@Injectable({
  providedIn: 'root'
})
export class FileService {

  constructor(private http: HttpClient) {
  }

  downloadFile(correlationId: string, file: FileUpload): void {
    this.http.get(`${environment.backendUrl}/api/upload/download/${file.id}?correlationId=${correlationId}`, {responseType: 'blob' as 'json'}).subscribe(
      (response: any) => {
        let dataType = response.type;
        let binaryData = [];
        binaryData.push(response);
        let downloadLink = document.createElement('a');
        downloadLink.href = window.URL.createObjectURL(new Blob(binaryData, {type: dataType}));
        downloadLink.setAttribute('download', file.originalFilename);
        document.body.appendChild(downloadLink);
        downloadLink.click();
      }
    )
  }
}
