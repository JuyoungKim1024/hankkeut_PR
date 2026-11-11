import type { JobSkill } from "@/lib/job-postings";

export type JobMatchResult = {
  score: number;
  matchedWeight: number;
  totalWeight: number;
  skills: (JobSkill & { matched: boolean })[];
  explanation: string;
  version: string;
  analyzedAt: string;
};

export type JobMatch =
  | { status: "READY"; result: JobMatchResult }
  | { status: "PROFILE_REQUIRED" | "JOB_SKILLS_REQUIRED" | "NOT_CALCULATED" | "STALE"; result: null };
