"use server";

import { revalidatePath } from "next/cache";
import {
  BackendApiError,
  createApplication as createRequest,
  deleteApplication as deleteRequest,
  updateApplication as updateRequest,
} from "@/lib/backend-api";
import type { ApplicationStatus } from "@/lib/applications";

export type ApplicationActionState = { error: string | null; message: string | null };
export const initialApplicationActionState: ApplicationActionState = { error: null, message: null };

export async function createApplication(
  _previousState: ApplicationActionState,
  formData: FormData,
): Promise<ApplicationActionState> {
  const jobPostingId = Number(formData.get("jobPostingId"));
  if (!Number.isInteger(jobPostingId) || jobPostingId < 1) {
    return { error: "지원 관리에 추가할 공고를 선택해 주세요.", message: null };
  }
  try {
    await createRequest({
      jobPostingId,
      status: applicationStatus(formData.get("status")),
      memo: optionalText(formData, "memo"),
      nextActionAt: optionalText(formData, "nextActionAt"),
    });
  } catch (error) {
    return failure(error, "지원 기록을 추가하지 못했습니다.");
  }
  revalidatePath("/applications");
  return { error: null, message: "지원 관리에 추가했습니다." };
}

export async function updateApplication(
  applicationId: number,
  _previousState: ApplicationActionState,
  formData: FormData,
): Promise<ApplicationActionState> {
  try {
    await updateRequest(applicationId, {
      status: applicationStatus(formData.get("status")),
      memo: optionalText(formData, "memo"),
      nextActionAt: optionalText(formData, "nextActionAt"),
    });
  } catch (error) {
    return failure(error, "지원 기록을 수정하지 못했습니다.");
  }
  revalidatePath("/applications");
  return { error: null, message: "변경사항을 저장했습니다." };
}

export async function removeApplication(
  applicationId: number,
  _previousState: ApplicationActionState,
  formData: FormData,
): Promise<ApplicationActionState> {
  if (formData.get("applicationId") !== String(applicationId)) {
    return { error: "삭제할 지원 기록을 다시 확인해 주세요.", message: null };
  }
  try {
    await deleteRequest(applicationId);
  } catch (error) {
    return failure(error, "지원 기록을 삭제하지 못했습니다.");
  }
  revalidatePath("/applications");
  return { error: null, message: null };
}

function applicationStatus(value: FormDataEntryValue | null): ApplicationStatus {
  const statuses: ApplicationStatus[] = ["INTERESTED", "PREPARING", "APPLIED", "INTERVIEWING", "ACCEPTED", "REJECTED", "WITHDRAWN"];
  return statuses.includes(value as ApplicationStatus) ? value as ApplicationStatus : "INTERESTED";
}

function optionalText(formData: FormData, name: string): string | undefined {
  const value = String(formData.get(name) ?? "").trim();
  return value || undefined;
}

function failure(error: unknown, fallback: string): ApplicationActionState {
  return { error: error instanceof BackendApiError ? error.message : fallback, message: null };
}
