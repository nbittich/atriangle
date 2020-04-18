export interface BackendInfo {
  artifact: string;
  group: string;
  name: string;
  version: string;
  time: Date;
  java: BackendInfoSource;
  encoding: BackendInfoEncoding;
}

export interface BackendInfoSource {
  source: string;
}

export interface BackendInfoEncoding {
  source: string;
  reporting: string;
}
