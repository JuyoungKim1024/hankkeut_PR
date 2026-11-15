"use client";

import { useState, useTransition } from "react";
import { submitExperience } from "@/app/resumes/[resumeId]/actions";
import type { ExperienceInput, ExperienceWorkspace } from "@/lib/resume-experiences";

const blank: ExperienceInput = { project: "", role: "", skills: "", outcome: "", evidence: "" };
const fields = [
  ["project", "프로젝트", 150, true], ["role", "역할", 150, true],
  ["skills", "기술", 300, false], ["outcome", "성과", 500, false],
] as const;
const inputClass = "w-full rounded-xl border border-ink-200 px-3 py-2";

export function ResumeExperienceEditor({ resumeId, workspace }: { resumeId: number; workspace: ExperienceWorkspace }) {
  const [input, setInput] = useState(blank);
  const [selected, setSelected] = useState<number | null>(null);
  const [pending, startTransition] = useTransition();
  const [error, setError] = useState("");
  const [notice, setNotice] = useState("");

  function run(action: "save" | "delete") {
    setError("");
    setNotice("");
    startTransition(async () => {
      const result = await submitExperience(resumeId, selected, input, action);
      if (result.error) return setError(result.error);
      setSelected(null);
      setInput(blank);
      setNotice(action === "delete" ? "경험을 삭제했습니다." : "확인한 경험을 저장했습니다.");
    });
  }

  if (!workspace.source) return <p>이력서에서 추출한 텍스트가 없습니다.</p>;
  return (
    <div className="space-y-6">
      <details className="rounded-xl border border-ink-200 p-4">
        <summary className="cursor-pointer font-bold">추출된 이력서 원문</summary>
        <pre className="mt-3 max-h-72 overflow-auto whitespace-pre-wrap break-words text-sm">{workspace.source}</pre>
      </details>
      <section className="space-y-2" aria-label="확인한 경험">
        <h2 className="font-bold">확인한 경험</h2>
        {workspace.experiences.length === 0 && <p className="text-sm">저장한 경험이 없습니다.</p>}
        {workspace.experiences.map(({ id, content }) => (
          <button key={id} type="button" disabled={pending} className="mr-2 rounded-xl border px-3 py-2"
            onClick={() => { setSelected(id); setInput(content); setError(""); setNotice(""); }}>{content.project} 편집</button>
        ))}
      </section>
      <form className="space-y-4" onSubmit={(event) => { event.preventDefault(); run("save"); }}>
        <fieldset disabled={pending} className="space-y-3">
          <label className="block">원문 근거
            <textarea className={inputClass} maxLength={1000} required value={input.evidence}
              onChange={(event) => setInput({ ...input, evidence: event.target.value })} />
          </label>
          {fields.map(([key, label, limit, required]) => (
            <label key={key} className="block">{label}
              <input className={inputClass} maxLength={limit} required={required} value={input[key]}
                onChange={(event) => setInput({ ...input, [key]: event.target.value })} />
            </label>
          ))}
          <button type="submit" className="rounded-xl bg-ink-950 px-4 py-2 text-white">확인하여 저장</button>
          {selected !== null && <button type="button" className="ml-2 rounded-xl border px-3 py-2" onClick={() => run("delete")}>선택한 경험 삭제</button>}
          {selected !== null && <button type="button" className="ml-2 px-3 py-2" onClick={() => { setSelected(null); setInput(blank); setError(""); }}>새 경험</button>}
        </fieldset>
        {error && <p role="alert" className="text-red-700">{error}</p>}
        <p role="status">{pending ? "처리 중…" : notice}</p>
      </form>
    </div>
  );
}
