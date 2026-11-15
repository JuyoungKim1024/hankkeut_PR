import { BackendUnavailable } from "@/components/backend-unavailable";
import Link from "next/link";
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
        <p className="mt-3 max-w-2xl text-sm leading-6 text-ink-500">저장한 이력서의 기술을 공개 채용공고와 비교하세요.</p>
        {resumes.length > 0 && <Link href="/jobs/recommendations" className="mt-3 inline-block text-sm font-bold underline">이력서 기반 추천</Link>}
      </header>
      <div className="mt-10"><ResumeManager resumes={resumes} /></div>
    </main>
  );
}
