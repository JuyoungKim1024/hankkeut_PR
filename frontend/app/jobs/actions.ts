"use server";

import { revalidatePath } from "next/cache";
import { redirect } from "next/navigation";
import { BackendApiError, importJobPosting } from "@/lib/backend-api";
import type { ManualJobPostingInput } from "@/lib/job-postings";

export type ImportJobPostingState = {
  error: string | null;
};

export const initialImportJobPostingState: ImportJobPostingState = { error: null };

export async function createJobPosting(
  _previousState: ImportJobPostingState,
  formData: FormData,
): Promise<ImportJobPostingState> {
  const input: ManualJobPostingInput = {
    originalUrl: requiredText(formData, "originalUrl"),
    companyName: requiredText(formData, "companyName"),
    title: requiredText(formData, "title"),
    jobCategory: requiredText(formData, "jobCategory"),
    career: optionalText(formData, "career"),
    location: optionalText(formData, "location"),
    description: requiredText(formData, "description"),
    qualification: optionalText(formData, "qualification"),
    preference: optionalText(formData, "preference"),
    postedAt: optionalText(formData, "postedAt"),
    expiredAt: optionalText(formData, "expiredAt"),
  };

  let jobPostingId: number;
  try {
    const result = await importJobPosting(input);
    jobPostingId = result.jobPostingId;
  } catch (error) {
    return {
      error: error instanceof BackendApiError ? error.message : "공고를 저장하지 못했습니다.",
    };
  }

  revalidatePath("/jobs");
  redirect(`/jobs/${jobPostingId}`);
}

function requiredText(formData: FormData, name: string): string {
  return String(formData.get(name) ?? "").trim();
}

function optionalText(formData: FormData, name: string): string | undefined {
  const value = requiredText(formData, name);
  return value || undefined;
}
