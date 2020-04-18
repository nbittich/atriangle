import {FileUpload} from "./file-upload";

export interface Project {
  id: string;
  name: string;
  description: string;
  creationDate: Date;
  lastModifiedDate?: Date;
  fileEvents: FileUpload[];
}
