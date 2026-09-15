import { ArrowUpRight, Bookmark, MapPin } from "lucide-react";
import type { JobRecommendation } from "@/lib/dashboard-data";

export function JobCard({ job }: { job: JobRecommendation }) {
  return (
    <article className="group rounded-2xl border border-ink-200 bg-white p-5 transition hover:-translate-y-0.5 hover:border-ink-950 hover:shadow-lg sm:p-6">
      <div className="flex items-start justify-between gap-4">
        <div>
          <div className="flex items-center gap-2">
            <p className="text-sm font-bold text-ink-600">{job.company}</p>
            {job.isNew ? (
              <span className="rounded-full bg-accent px-2 py-0.5 font-mono-label text-[9px] font-bold uppercase">New</span>
            ) : null}
          </div>
          <h3 className="mt-2 text-lg font-extrabold tracking-tight text-ink-950">{job.title}</h3>
        </div>
        <button
          type="button"
          aria-label={`${job.company} 공고 저장`}
          className="grid h-9 w-9 shrink-0 place-items-center rounded-full border border-ink-200 text-ink-500 hover:border-ink-950 hover:text-ink-950"
        >
          <Bookmark aria-hidden="true" className="h-4 w-4" />
        </button>
      </div>

      <div className="mt-4 flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-ink-500">
        <span className="inline-flex items-center gap-1">
          <MapPin aria-hidden="true" className="h-3.5 w-3.5" />
          {job.location}
        </span>
        <span aria-hidden="true">·</span>
        <span>{job.career}</span>
        <span aria-hidden="true">·</span>
        <span className="font-bold text-ink-950">{job.deadline}</span>
      </div>

      <div className="mt-6 grid grid-cols-[76px_minmax(0,1fr)] gap-4 border-t border-ink-100 pt-5">
        <div>
          <p className="font-mono-label text-[9px] uppercase text-ink-400">Match</p>
          <p className="mt-1 text-2xl font-black tracking-tightest">{job.score}%</p>
        </div>
        <div className="min-w-0">
          <div className="flex flex-wrap gap-1.5">
            {job.matchedSkills.map((skill) => (
              <span key={skill} className="rounded-md bg-ink-100 px-2 py-1 text-[10px] font-semibold text-ink-600">
                {skill}
              </span>
            ))}
            {job.missingSkills.map((skill) => (
              <span key={skill} className="rounded-md border border-dashed border-ink-300 px-2 py-1 text-[10px] font-semibold text-ink-500">
                + {skill}
              </span>
            ))}
          </div>
          <a href="#" className="mt-3 inline-flex items-center gap-1 text-xs font-bold text-ink-950">
            분석 결과 보기
            <ArrowUpRight aria-hidden="true" className="h-3.5 w-3.5" />
          </a>
        </div>
      </div>
    </article>
  );
}
