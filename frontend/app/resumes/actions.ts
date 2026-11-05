"use server";

import { revalidatePath } from "next/cache";
import {
  BackendApiError,
  deleteResume as deleteResumeRequest,
  uploadResume as uploadResumeRequest,
} from "@/lib/backend-api";

export type ResumeActionState = {
  error: string | null;
  message: string | null;
};

export const initialResumeActionState: ResumeActionState = { error: null, message: null };

export async function uploadResume(
  _previousState: ResumeActionState,
  formData: FormData,
): Promise<ResumeActionState> {
  const file = formData.get("file");
  if (!(file instanceof File) || file.size === 0) {
    return { error: "PDF 또는 DOCX 파일을 선택해 주세요.", message: null };
  }

  try {
    await uploadResumeRequest(file);
  } catch (error) {
    return {
      error: error instanceof BackendApiError ? error.message : "이력서를 업로드하지 못했습니다.",
      message: null,
    };
  }

  revalidatePath("/resumes");
  return { error: null, message: "이력서를 저장하고 텍스트를 추출했습니다." };
}

export async function removeResume(
  resumeId: number,
  _previousState: ResumeActionState,
  formData: FormData,
): Promise<ResumeActionState> {
  if (formData.get("resumeId") !== String(resumeId)) {
    return { error: "삭제할 이력서를 다시 확인해 주세요.", message: null };
  }
  try {
    await deleteResumeRequest(resumeId);
  } catch (error) {
    return {
      error: error instanceof BackendApiError ? error.message : "이력서를 삭제하지 못했습니다.",
      message: null,
    };
  }

  revalidatePath("/resumes");
  return { error: null, message: null };
}
