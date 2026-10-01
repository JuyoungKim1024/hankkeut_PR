import { ArrowUpRight, CalendarDays, MapPin } from "lucide-react";
import Link from "next/link";
import { formatDate } from "@/lib/format";
import type { JobPostingSummary } from "@/lib/job-postings";

export function JobListCard({ job }: { job: JobPostingSummary }) {
  return (
    <article className="group rounded-2xl border border-ink-200 bg-white p-5 transition hover:-translate-y-0.5 hover:border-ink-950 hover:shadow-lg sm:p-6">
      <div className="flex items-start justify-between gap-5">
        <div className="min-w-0">
          <div className="flex flex-wrap items-center gap-2">
            <p className="truncate text-sm font-bold text-ink-600">{job.companyName}</p>
            <span className={`rounded-full px-2 py-0.5 font-mono-label text-[9px] font-bold uppercase ${job.status === "ACTIVE" ? "bg-accent text-ink-950" : "bg-ink-100 text-ink-500"}`}>
              {job.status === "ACTIVE" ? "Open" : "Closed"}
            </span>
          </div>
          <h2 className="mt-2 text-lg font-extrabold tracking-tight text-ink-950 sm:text-xl">{job.title}</h2>
        </div>
        <Link
          href={`/jobs/${job.id}`}
          aria-label={`${job.companyName} ${job.title} 상세 보기`}
          className="grid h-10 w-10 shrink-0 place-items-center rounded-full border border-ink-200 text-ink-500 transition group-hover:border-ink-950 group-hover:text-ink-950"
        >
          <ArrowUpRight aria-hidden="true" className="h-4 w-4" />
        </Link>
      </div>

      <div className="mt-5 flex flex-wrap gap-2 text-xs font-semibold text-ink-600">
        <span className="rounded-lg bg-ink-100 px-2.5 py-1.5">{job.jobCategory}</span>
        {job.career ? <span className="rounded-lg bg-ink-100 px-2.5 py-1.5">{job.career}</span> : null}
      </div>

      <div className="mt-6 flex flex-wrap items-center gap-x-5 gap-y-2 border-t border-ink-100 pt-4 text-xs text-ink-500">
        <span className="inline-flex items-center gap-1.5">
          <MapPin aria-hidden="true" className="h-3.5 w-3.5" />
          {job.location ?? "지역 미정"}
        </span>
        <span className="inline-flex items-center gap-1.5">
          <CalendarDays aria-hidden="true" className="h-3.5 w-3.5" />
          마감 {formatDate(job.expiredAt)}
        </span>
      </div>
    </article>
  );
}
