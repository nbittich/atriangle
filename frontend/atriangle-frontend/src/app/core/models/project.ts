import {FileUpload} from "./file-upload";

export class Project {
  id: string;
  name: string;
  creationDate: Date;
  lastModifiedDate?: Date;
  fileEvents: FileUpload[];
}
