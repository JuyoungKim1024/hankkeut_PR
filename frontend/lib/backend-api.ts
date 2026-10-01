import type {
  JobPostingDetail,
  JobPostingPage,
  JobPostingSearch,
  ManualJobPostingImportResult,
  ManualJobPostingInput,
} from "@/lib/job-postings";
import type { Profile } from "@/lib/profile";

type ApiError = {
  code: string;
  message: string;
};

type ApiResponse<T> = {
  success: boolean;
  data?: T;
  error?: ApiError;
};

export class BackendApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
  ) {
    super(message);
  }
}

const backendBaseUrl = (process.env.BACKEND_BASE_URL ?? "http://localhost:8080").replace(/\/$/, "");

export async function getJobPostings(search: JobPostingSearch): Promise<JobPostingPage> {
  const params = new URLSearchParams();
  if (search.query) params.set("query", search.query);
  if (search.location) params.set("location", search.location);
  if (search.status) params.set("status", search.status);
  if (search.sort) params.set("sort", search.sort);
  if (search.page !== undefined) params.set("page", String(search.page));
  if (search.size !== undefined) params.set("size", String(search.size));

  return request<JobPostingPage>(`/api/jobs?${params.toString()}`);
}

export async function getJobPosting(jobId: number): Promise<JobPostingDetail> {
  return request<JobPostingDetail>(`/api/jobs/${jobId}`);
}

export async function importJobPosting(input: ManualJobPostingInput): Promise<ManualJobPostingImportResult> {
  return request<ManualJobPostingImportResult>("/api/jobs/imports", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
}

export async function getProfile(): Promise<Profile | null> {
  try {
    return await request<Profile>("/api/profile");
  } catch (error) {
    if (error instanceof BackendApiError && error.status === 404 && error.code === "PROFILE_NOT_FOUND") {
      return null;
    }
    throw error;
  }
}

export async function saveProfile(input: Profile): Promise<Profile> {
  return request<Profile>("/api/profile", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(input),
  });
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let response: Response;
  try {
    response = await fetch(`${backendBaseUrl}${path}`, { ...init, cache: "no-store" });
  } catch {
    throw new BackendApiError(503, "BACKEND_UNAVAILABLE", "백엔드 서버에 연결할 수 없습니다.");
  }

  const body = (await response.json()) as ApiResponse<T>;
  if (!response.ok || !body.success || body.data === undefined) {
    throw new BackendApiError(
      response.status,
      body.error?.code ?? "BACKEND_ERROR",
      body.error?.message ?? "요청을 처리하지 못했습니다.",
    );
  }
  return body.data;
}
