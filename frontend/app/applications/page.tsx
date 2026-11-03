import { ApplicationManager } from "@/components/application-manager";
import { BackendUnavailable } from "@/components/backend-unavailable";
import { BackendApiError, getApplications, getJobPostings } from "@/lib/backend-api";
import type { ApplicationRecord } from "@/lib/applications";
import type { JobPostingPage } from "@/lib/job-postings";

export default async function ApplicationsPage() {
  let applications: ApplicationRecord[];
  let jobs: JobPostingPage;
  try {
    [applications, jobs] = await Promise.all([
      getApplications(),
      getJobPostings({ status: "ACTIVE", size: 100 }),
    ]);
  } catch (error) {
    if (error instanceof BackendApiError) return <BackendUnavailable />;
    throw error;
  }

  return (
    <main className="mx-auto max-w-[1320px] px-5 pb-28 pt-8 sm:px-8 lg:px-10 lg:pb-14 lg:pt-12">
      <header>
        <p className="font-mono-label text-[10px] uppercase text-ink-400">Application tracker</p>
        <h1 className="mt-2 text-3xl font-black tracking-tightest sm:text-4xl">지원 관리</h1>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-ink-500">관심 공고부터 결과까지 현재 단계와 다음 행동을 한곳에서 관리하세요.</p>
      </header>
      <div className="mt-10"><ApplicationManager applications={applications} jobs={jobs.items} /></div>
    </main>
  );
}
