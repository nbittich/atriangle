export interface SparqlQueryRequest {
  projectId: string;
  variables?: Map<string, string>;
  freemarkerTemplateFileId: string;
  type: SparqlQueryRequestType;
}

export enum SparqlQueryRequestType {
  ASK_QUERY, SELECT_QUERY, CONSTRUCT_QUERY
}
