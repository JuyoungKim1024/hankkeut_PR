"use server";

import { revalidatePath } from "next/cache";
import { BackendApiError, deleteResumeExperience, saveResumeExperience } from "@/lib/backend-api";
import type { ExperienceInput } from "@/lib/resume-experiences";

export async function submitExperience(resumeId: number, experienceId: number | null, input: ExperienceInput, action: "save" | "delete") {
  try {
    if (action === "delete") {
      if (!experienceId) return { error: "삭제할 경험을 선택해 주세요." };
      await deleteResumeExperience(resumeId, experienceId);
    } else {
      await saveResumeExperience(resumeId, experienceId, input);
    }
    revalidatePath(`/resumes/${resumeId}`);
    return {};
  } catch (error) {
    return { error: error instanceof BackendApiError ? error.message : "경험을 처리하지 못했습니다." };
  }
}
