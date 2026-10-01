export type JobPostingStatus = "ACTIVE" | "CLOSED";
export type JobPostingSort = "LATEST" | "DEADLINE";

export type JobPostingSummary = {
  id: number;
  source: "MANUAL";
  companyName: string;
  title: string;
  jobCategory: string;
  career: string | null;
  location: string | null;
  originalUrl: string;
  postedAt: string | null;
  expiredAt: string | null;
  status: JobPostingStatus;
};

export type JobPostingDetail = JobPostingSummary & {
  description: string;
  qualification: string | null;
  preference: string | null;
};

export type JobPostingPage = {
  items: JobPostingSummary[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export type JobPostingSearch = {
  query?: string;
  location?: string;
  status?: JobPostingStatus;
  sort?: JobPostingSort;
  page?: number;
  size?: number;
};

export type ManualJobPostingInput = {
  originalUrl: string;
  companyName: string;
  title: string;
  jobCategory: string;
  career?: string;
  location?: string;
  description: string;
  qualification?: string;
  preference?: string;
  postedAt?: string;
  expiredAt?: string;
};

export type ManualJobPostingImportResult = {
  jobPostingId: number;
  created: number;
  updated: number;
  unchanged: number;
};
