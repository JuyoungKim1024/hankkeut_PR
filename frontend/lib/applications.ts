export type ApplicationStatus =
  | "INTERESTED"
  | "PREPARING"
  | "APPLIED"
  | "INTERVIEWING"
  | "ACCEPTED"
  | "REJECTED"
  | "WITHDRAWN";

export type ApplicationRecord = {
  id: number;
  jobPostingId: number;
  companyName: string;
  jobTitle: string;
  location: string | null;
  status: ApplicationStatus;
  memo: string | null;
  nextActionAt: string | null;
  appliedAt: string | null;
  updatedAt: string;
};

export const applicationStatusLabels: Record<ApplicationStatus, string> = {
  INTERESTED: "관심",
  PREPARING: "준비",
  APPLIED: "지원",
  INTERVIEWING: "면접",
  ACCEPTED: "합격",
  REJECTED: "불합격",
  WITHDRAWN: "철회",
};

export function allowedStatuses(current: ApplicationStatus): ApplicationStatus[] {
  const transitions: Record<ApplicationStatus, ApplicationStatus[]> = {
    INTERESTED: ["INTERESTED", "PREPARING", "WITHDRAWN"],
    PREPARING: ["PREPARING", "INTERESTED", "APPLIED", "WITHDRAWN"],
    APPLIED: ["APPLIED", "INTERVIEWING", "REJECTED", "WITHDRAWN"],
    INTERVIEWING: ["INTERVIEWING", "ACCEPTED", "REJECTED", "WITHDRAWN"],
    ACCEPTED: ["ACCEPTED"],
    REJECTED: ["REJECTED"],
    WITHDRAWN: ["WITHDRAWN"],
  };
  return transitions[current];
}
