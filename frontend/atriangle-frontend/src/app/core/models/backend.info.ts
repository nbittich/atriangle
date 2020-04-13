export class BackendInfo {
  artifact: string;
  group: string;
  name: string;
  version: string;
  time: Date;
  java: BackendInfoSource;
  encoding: BackendInfoEncoding;
}

export class BackendInfoSource {
  source: string;
}

export class BackendInfoEncoding {
  source: string;
  reporting: string;
}
