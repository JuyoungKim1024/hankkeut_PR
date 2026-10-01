import { BackendUnavailable } from "@/components/backend-unavailable";
import { ResumeManager } from "@/components/resume-manager";
import { BackendApiError, getResumes } from "@/lib/backend-api";
import type { ResumeSummary } from "@/lib/resumes";

export default async function ResumesPage() {
  let resumes: ResumeSummary[];
  try {
    resumes = await getResumes();
  } catch (error) {
    if (error instanceof BackendApiError) return <BackendUnavailable />;
    throw error;
  }

  return (
    <main className="mx-auto max-w-5xl px-5 pb-28 pt-8 sm:px-8 lg:px-10 lg:pb-14 lg:pt-12">
      <header>
        <p className="font-mono-label text-[10px] uppercase text-ink-400">Resume library</p>
        <h1 className="mt-2 text-3xl font-black tracking-tightest sm:text-4xl">이력서</h1>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-ink-500">원본 파일과 추출한 텍스트는 이 컴퓨터에만 저장됩니다. 실제 작성한 경험을 이후 공고 분석의 기준으로 사용합니다.</p>
      </header>
      <div className="mt-10"><ResumeManager resumes={resumes} /></div>
    </main>
  );
}
