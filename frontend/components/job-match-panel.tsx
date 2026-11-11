"use client";

import Link from "next/link";
import { useActionState } from "react";
import { runJobMatch } from "@/app/jobs/[jobId]/match-actions";
import type { JobMatch, JobMatchResult } from "@/lib/job-match";

export function JobMatchPanel({ jobId, match }: { jobId: number; match: JobMatch }) {
  const [state, action, pending] = useActionState(runJobMatch.bind(null, jobId), { error: null });
  const canCalculate = match.status === "NOT_CALCULATED" || match.status === "STALE" || match.status === "READY";

  return (
    <section aria-labelledby="job-match-heading" className="space-y-4 border-t border-ink-100 pt-8">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h2 id="job-match-heading" className="text-lg font-black tracking-tight">기술 적합도</h2>
        {canCalculate && (
          <form action={action}>
            <button
              type="submit"
              disabled={pending}
              className="rounded-xl bg-ink-950 px-4 py-2 text-sm font-bold text-white disabled:opacity-60"
            >
              {pending ? "계산 중…" : match.status === "NOT_CALCULATED" ? "적합도 계산" : "다시 계산"}
            </button>
          </form>
        )}
      </div>
      {state.error && <p role="alert" className="text-sm text-red-700">{state.error}</p>}
      {match.status === "PROFILE_REQUIRED" && (
        <p className="text-sm text-ink-700">
          <Link href="/profile" className="font-bold underline">프로필에 보유 기술을 등록</Link>하면 공고와 비교할 수 있습니다.
        </p>
      )}
      {match.status === "JOB_SKILLS_REQUIRED" && (
        <p className="text-sm text-ink-700">
          <a href="#job-skills-heading" className="font-bold underline">공고의 기술 분석 결과</a>가 있어야 적합도를 계산할 수 있습니다.
        </p>
      )}
      {match.status === "NOT_CALCULATED" && <p className="text-sm text-ink-700">보유 기술과 공고의 필수·우대 기술을 비교합니다.</p>}
      {match.status === "STALE" && <p role="status" className="text-sm text-ink-700">비교에 사용한 기술이 바뀌었습니다. 적합도를 다시 계산해 주세요.</p>}
      {match.status === "READY" && <MatchResult result={match.result} />}
    </section>
  );
}

function MatchResult({ result }: { result: JobMatchResult }) {
  const matched = result.skills.filter((skill) => skill.matched);
  const missingRequired = result.skills.filter((skill) => !skill.matched && skill.type === "REQUIRED");
  const missingPreferred = result.skills.filter((skill) => !skill.matched && skill.type === "PREFERRED");

  return (
    <div className="space-y-5 rounded-2xl bg-ink-50 p-4 sm:p-5">
      <div className="space-y-1" aria-live="polite">
        <p className="text-2xl font-black text-ink-950">{result.score}점 <span className="text-sm font-medium text-ink-500">/ 100점</span></p>
        <p className="text-sm text-ink-700">{result.explanation}</p>
        <p className="text-xs text-ink-500">계산 시각 <time dateTime={result.analyzedAt}>{result.analyzedAt.replace("T", " ").slice(0, 16)}</time></p>
      </div>
      <div className="grid gap-4 sm:grid-cols-3">
        <MatchSkillGroup title="일치 기술" skills={matched} />
        <MatchSkillGroup title="부족한 필수 기술" skills={missingRequired} />
        <MatchSkillGroup title="부족한 우대 기술" skills={missingPreferred} />
      </div>
    </div>
  );
}

function MatchSkillGroup({ title, skills }: { title: string; skills: JobMatchResult["skills"] }) {
  return (
    <div className="space-y-2">
      <h3 className="text-sm font-bold">{title}</h3>
      {skills.length > 0 ? (
        <ul className="flex flex-wrap gap-2">
          {skills.map(({ name }) => <li key={name} className="rounded-lg bg-white px-3 py-1 text-sm">{name}</li>)}
        </ul>
      ) : <p className="text-sm text-ink-500">없음</p>}
    </div>
  );
}
