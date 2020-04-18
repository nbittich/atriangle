export interface LogEvent {
  correlationId: string;
  message: string;
  creationDate: Date;
  type: LogEventType;
}

export enum LogEventType {
  INFO, ERROR
}
