export const REPORT_TARGET_TYPES = ["TRAVEL", "MANAGER", "TRAVELER"] as const;
export type ReportTargetType = (typeof REPORT_TARGET_TYPES)[number];

export const REPORT_STATUSES = ["OPEN", "REVIEWED", "ACTIONED", "DISMISSED"] as const;
export type ReportStatus = (typeof REPORT_STATUSES)[number];

export type ReportResponse = {
  id: string;
  reporterUserId: string;
  targetType: ReportTargetType;
  targetId: string;
  reason: string;
  status: ReportStatus;
  createdAt: string;
};

export type ReportCreateInput = {
  targetType: ReportTargetType;
  targetId: string;
  reason: string;
};
