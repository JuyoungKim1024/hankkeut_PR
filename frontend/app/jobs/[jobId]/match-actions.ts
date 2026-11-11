"use server";

import { revalidatePath } from "next/cache";
import { BackendApiError, calculateJobMatch } from "@/lib/backend-api";

export type MatchActionState = { error: string | null };

export async function runJobMatch(jobId: number): Promise<MatchActionState> {
  if (!Number.isSafeInteger(jobId) || jobId < 1) {
    return { error: "유효한 공고를 선택해 주세요." };
  }
  try {
    await calculateJobMatch(jobId);
  } catch (error) {
    return { error: error instanceof BackendApiError ? error.message : "적합도를 계산하지 못했습니다." };
  }
  revalidatePath(`/jobs/${jobId}`);
  return { error: null };
}
