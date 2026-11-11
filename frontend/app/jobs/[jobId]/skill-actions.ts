"use server";

import { revalidatePath } from "next/cache";
import { analyzeJobSkills, BackendApiError } from "@/lib/backend-api";

export type SkillAnalysisState = {
  error: string | null;
  completed: boolean;
};

export async function runSkillAnalysis(jobId: number): Promise<SkillAnalysisState> {
  if (!Number.isSafeInteger(jobId) || jobId < 1) {
    return { error: "유효한 공고를 선택해 주세요.", completed: false };
  }

  try {
    await analyzeJobSkills(jobId);
  } catch (error) {
    return {
      error: error instanceof BackendApiError ? error.message : "기술 분석을 완료하지 못했습니다.",
      completed: false,
    };
  }
  revalidatePath(`/jobs/${jobId}`);
  return { error: null, completed: true };
}
