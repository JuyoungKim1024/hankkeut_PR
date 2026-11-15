import Link from "next/link";
import { BackendUnavailable } from "@/components/backend-unavailable";
import { BackendApiError, getResumeJobResults, getResumes } from "@/lib/backend-api";
import type { ResumeJobResults } from "@/lib/resume-job-results";

export default async function ResumeRecommendationsPage({
  searchParams,
}: {
  searchParams: Promise<{ resumeId?: string; board?: string }>;
}) {
  const query = await searchParams;
  let resumes;
  try {
    resumes = await getResumes();
  } catch (error) {
    if (error instanceof BackendApiError) return <BackendUnavailable />;
    throw error;
  }
  const id = Number(query.resumeId);
  const board = typeof query.board === "string" ? query.board.trim() : "";
  let results: ResumeJobResults | undefined;
  let message: string | undefined;
  if (board && resumes.some((resume) => resume.id === id)) {
    try {
      results = await getResumeJobResults(id, board);
    } catch (error) {
      if (!(error instanceof BackendApiError)) throw error;
      message = error.message;
    }
  }

  return (
    <main className="mx-auto max-w-5xl space-y-6 px-5 pb-28 pt-8 sm:px-8">
      <Link href="/jobs" className="text-sm font-bold underline">공고 보관함</Link>
      <h1 className="text-3xl font-black">이력서 기반 공고 추천</h1>
      {resumes.length ? (
        <form className="space-y-4 rounded-xl border border-ink-200 bg-white p-4" method="get">
          <label className="block">이력서
            <select name="resumeId" defaultValue={resumes.some((resume) => resume.id === id) ? id : resumes[0].id}
              className="mt-1 block w-full rounded-lg border p-2">
              {resumes.map((resume) => <option key={resume.id} value={resume.id}>{resume.originalFileName}</option>)}
            </select>
          </label>
          <label className="block">Greenhouse 회사 보드
            <input name="board" defaultValue={board} placeholder="예: greenhouse" maxLength={80}
              required className="mt-1 block w-full rounded-lg border p-2" />
          </label>
          <p className="text-sm text-ink-600">회사 채용 주소가 job-boards.greenhouse.io/회사이름 형식이면 회사이름만 입력하세요.</p>
          <button className="rounded-xl bg-ink-950 px-4 py-2 font-bold text-white">공고 불러오기</button>
        </form>
      ) : <p><Link href="/resumes" className="font-bold underline">이력서를 먼저 등록하세요.</Link></p>}
      {message && <p role="alert" className="text-red-700">{message}</p>}
      {results && (
        <section aria-label="추천 결과" className="space-y-4">
          <p>비교한 기술 {results.detectedSkills.join(", ") || "없음"}</p>
          {results.items.length === 0 && <p>이력서와 겹치는 기술을 확인한 공고가 없습니다.</p>}
          {results.items.map((job) => (
            <article key={job.url} className="space-y-2 rounded-xl border border-ink-200 bg-white p-4">
              <h2 className="font-bold"><a href={job.url} target="_blank" rel="noreferrer" className="underline">{job.title}</a></h2>
              <p className="text-sm">{job.location} · 기술 키워드 일치율 {job.score}%</p>
              <p className="text-sm">겹치는 기술 {job.matchedSkills.join(", ")}</p>
              {job.missingSkills.length > 0 && <p className="text-sm">이력서에서 확인되지 않은 기술 {job.missingSkills.join(", ")}</p>}
            </article>
          ))}
          {results.items.length > 0 && <p className="text-sm text-ink-600">경력·근무지·지원 자격은 공고 원문에서 확인하세요.</p>}
        </section>
      )}
    </main>
  );
}
