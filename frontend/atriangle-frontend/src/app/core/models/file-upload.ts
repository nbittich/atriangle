export class FileUpload {
  id: string;
  contentType: string;
  eventType: FileUploadType;
  originalFilename: string;
  name: string;
  size: number;
  creationDate: Date;
  lastModifiedDate: Date;
}

export enum FileUploadType {
  RAW_FILE,
  PROJECT_FILE,
  FREEMARKER_TEMPLATE_FILE,
  SKOS_PLAY_CONVERTER_OUTPUT,
  RDF_TO_JSON_LD_OUTPUT,
  RDF_FILE,
  SHACL_FILE,
}
