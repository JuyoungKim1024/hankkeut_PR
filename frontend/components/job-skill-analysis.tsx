"use client";

import { useActionState } from "react";
import { runSkillAnalysis, type SkillAnalysisState } from "@/app/jobs/[jobId]/skill-actions";
import type { JobSkill } from "@/lib/job-postings";

const initialState: SkillAnalysisState = { error: null, completed: false };

export function JobSkillAnalysis({ jobId, skills }: { jobId: number; skills: JobSkill[] }) {
  const [state, action, pending] = useActionState(runSkillAnalysis.bind(null, jobId), initialState);
  const required = skills.filter((skill) => skill.type === "REQUIRED");
  const preferred = skills.filter((skill) => skill.type === "PREFERRED");

  return (
    <section aria-labelledby="job-skills-heading" className="space-y-4 border-t border-ink-100 pt-8">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <h2 id="job-skills-heading" className="text-lg font-black tracking-tight">기술 분석</h2>
        <form action={action}>
          <button
            type="submit"
            disabled={pending}
            className="rounded-xl bg-ink-950 px-4 py-2 text-sm font-bold text-white disabled:opacity-60"
          >
            {pending ? "분석 중…" : skills.length > 0 ? "다시 분석" : "기술 분석 실행"}
          </button>
        </form>
      </div>
      {state.error && <p role="alert" className="text-sm text-red-700">{state.error}</p>}
      {state.completed && (
        <p role="status" className="text-sm text-ink-700">
          {skills.length > 0 ? "기술 분석을 완료했습니다." : "공고에서 추출할 수 있는 기술이 없습니다."}
        </p>
      )}
      {skills.length === 0 && !state.completed && (
        <p className="text-sm text-ink-700">분석을 실행하면 공고에서 찾은 기술을 확인할 수 있습니다.</p>
      )}
      {skills.length > 0 && (
        <div className="grid gap-5 sm:grid-cols-2">
          <SkillGroup title="필수 기술" skills={required} />
          <SkillGroup title="우대 기술" skills={preferred} />
        </div>
      )}
    </section>
  );
}

function SkillGroup({ title, skills }: { title: string; skills: JobSkill[] }) {
  return (
    <div className="space-y-2">
      <h3 className="text-sm font-bold">{title}</h3>
      {skills.length === 0 ? (
        <p className="text-sm text-ink-500">추출된 기술이 없습니다.</p>
      ) : (
        <ul className="flex flex-wrap gap-2">
          {skills.map(({ name }) => (
            <li key={name} className="rounded-lg bg-ink-100 px-3 py-1 text-sm text-ink-950">{name}</li>
          ))}
        </ul>
      )}
    </div>
  );
}
