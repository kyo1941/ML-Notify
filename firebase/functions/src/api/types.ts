export type TaskStatus = "START" | "FINISH";

export interface SendNotificationRequestBody {
  processId: string;
  status: TaskStatus;
  deviceToken: string;
  messageTitle?: string;
  messageBody?: string;
  taskName?: string;
}