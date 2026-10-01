import { ArrowLeft, ArrowUpRight, CalendarDays, MapPin } from "lucide-react";
import Link from "next/link";
import { notFound } from "next/navigation";
import { BackendApiError, getJobPosting } from "@/lib/backend-api";
import { formatDate } from "@/lib/format";

export default async function JobPostingDetailPage({ params }: { params: Promise<{ jobId: string }> }) {
  const { jobId } = await params;
  const parsedId = Number.parseInt(jobId, 10);
  if (!Number.isInteger(parsedId) || parsedId < 1) notFound();

  let job;
  try {
    job = await getJobPosting(parsedId);
  } catch (error) {
    if (error instanceof BackendApiError && error.status === 404) notFound();
    throw error;
  }

  return (
    <main className="mx-auto max-w-5xl px-5 pb-28 pt-8 sm:px-8 lg:px-10 lg:pb-14 lg:pt-12">
      <Link href="/jobs" className="inline-flex items-center gap-2 text-sm font-bold text-ink-500 hover:text-ink-950">
        <ArrowLeft aria-hidden="true" className="h-4 w-4" />
        공고 보관함
      </Link>

      <article className="mt-8 overflow-hidden rounded-3xl border border-ink-200 bg-white">
        <header className="border-b border-ink-100 p-6 sm:p-9">
          <div className="flex flex-wrap items-center gap-2">
            <p className="text-sm font-bold text-ink-500">{job.companyName}</p>
            <span className={`rounded-full px-2.5 py-1 font-mono-label text-[9px] font-bold uppercase ${job.status === "ACTIVE" ? "bg-accent" : "bg-ink-100 text-ink-500"}`}>
              {job.status === "ACTIVE" ? "Open" : "Closed"}
            </span>
          </div>
          <h1 className="mt-3 text-3xl font-black tracking-tightest sm:text-4xl">{job.title}</h1>
          <div className="mt-6 flex flex-wrap gap-x-5 gap-y-2 text-sm text-ink-500">
            <span className="inline-flex items-center gap-1.5"><MapPin aria-hidden="true" className="h-4 w-4" />{job.location ?? "지역 미정"}</span>
            <span className="inline-flex items-center gap-1.5"><CalendarDays aria-hidden="true" className="h-4 w-4" />마감 {formatDate(job.expiredAt)}</span>
          </div>
          <div className="mt-5 flex flex-wrap gap-2">
            <span className="rounded-lg bg-ink-100 px-3 py-1.5 text-xs font-bold">{job.jobCategory}</span>
            {job.career ? <span className="rounded-lg bg-ink-100 px-3 py-1.5 text-xs font-bold">{job.career}</span> : null}
          </div>
        </header>

        <div className="space-y-10 p-6 sm:p-9">
          <ContentSection title="공고 내용" content={job.description} />
          <ContentSection title="자격요건" content={job.qualification} />
          <ContentSection title="우대사항" content={job.preference} />
        </div>

        <footer className="flex flex-col gap-4 border-t border-ink-100 bg-ink-50 p-6 sm:flex-row sm:items-center sm:justify-between sm:px-9">
          <div className="text-xs text-ink-500">
            <p>게시 {formatDate(job.postedAt)}</p>
            <p className="mt-1">직접 등록한 공고</p>
          </div>
          <a href={job.originalUrl} target="_blank" rel="noreferrer" className="inline-flex items-center justify-center gap-2 rounded-xl bg-ink-950 px-5 py-3 text-sm font-bold text-white">
            원문 열기
            <ArrowUpRight aria-hidden="true" className="h-4 w-4" />
          </a>
        </footer>
      </article>
    </main>
  );
}

function ContentSection({ title, content }: { title: string; content: string | null }) {
  return (
    <section>
      <h2 className="text-lg font-black tracking-tight">{title}</h2>
      <p className="mt-4 whitespace-pre-wrap text-sm leading-7 text-ink-700">{content || "등록된 내용이 없습니다."}</p>
    </section>
  );
}
