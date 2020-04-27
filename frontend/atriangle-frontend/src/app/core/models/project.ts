import {FileUpload} from "./file-upload";
import {SparqlQueryRequest} from "./sparql.query.request";

export interface Project {
  id: string;
  name: string;
  description: string;
  creationDate: Date;
  lastModifiedDate?: Date;
  fileEvents: FileUpload[];
  sparqlQueries: SparqlQueryRequest[];
}
