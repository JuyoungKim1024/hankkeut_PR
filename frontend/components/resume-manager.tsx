"use client";

import { FileText, Trash2, Upload } from "lucide-react";
import { useActionState } from "react";
import {
  initialResumeActionState,
  removeResume,
  uploadResume,
} from "@/app/resumes/actions";
import { formatDate } from "@/lib/format";
import type { ResumeSummary } from "@/lib/resumes";

export function ResumeManager({ resumes }: { resumes: ResumeSummary[] }) {
  const [state, action, pending] = useActionState(uploadResume, initialResumeActionState);

  return (
    <div className="space-y-8">
      <form action={action} className="rounded-3xl border border-ink-200 bg-white p-5 sm:p-8">
        <div className="flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between">
          <div>
            <h2 className="text-lg font-black">새 이력서 등록</h2>
            <p className="mt-2 text-sm leading-6 text-ink-500">PDF 또는 DOCX, 최대 10MB까지 등록할 수 있습니다.</p>
          </div>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            <input
              type="file"
              name="file"
              required
              accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
              className="max-w-full text-sm text-ink-500 file:mr-3 file:rounded-lg file:border-0 file:bg-ink-100 file:px-3 file:py-2 file:text-xs file:font-bold file:text-ink-700"
            />
            <button type="submit" disabled={pending} className="inline-flex shrink-0 items-center justify-center gap-2 rounded-xl bg-ink-950 px-4 py-3 text-sm font-bold text-white disabled:opacity-50">
              <Upload aria-hidden="true" className="h-4 w-4" />
              {pending ? "처리 중..." : "업로드"}
            </button>
          </div>
        </div>
        <div aria-live="polite" className="mt-4">
          {state.error ? <p role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{state.error}</p> : null}
          {state.message ? <p className="rounded-xl bg-lime-50 px-4 py-3 text-sm font-semibold text-lime-800">{state.message}</p> : null}
        </div>
      </form>

      <section>
        <div className="flex items-center justify-between">
          <h2 className="text-lg font-black">저장된 이력서</h2>
          <span className="font-mono-label text-xs text-ink-400">{resumes.length} files</span>
        </div>
        {resumes.length > 0 ? (
          <div className="mt-4 space-y-3">
            {resumes.map((resume) => (
              <article key={resume.id} className="flex flex-col gap-4 rounded-2xl border border-ink-200 bg-white p-5 sm:flex-row sm:items-center sm:justify-between">
                <div className="flex min-w-0 items-center gap-4">
                  <span className="grid h-11 w-11 shrink-0 place-items-center rounded-xl bg-ink-100"><FileText aria-hidden="true" className="h-5 w-5" /></span>
                  <div className="min-w-0">
                    <h3 className="truncate text-sm font-extrabold">{resume.originalFileName}</h3>
                    <p className="mt-1 text-xs text-ink-500">{resume.fileType} · 추출 {resume.extractedTextLength.toLocaleString("ko-KR")}자 · {formatDate(resume.createdAt)}</p>
                  </div>
                </div>
                <DeleteResumeForm resumeId={resume.id} fileName={resume.originalFileName} />
              </article>
            ))}
          </div>
        ) : (
          <div className="mt-4 rounded-3xl border border-dashed border-ink-300 bg-white px-6 py-16 text-center">
            <p className="font-bold">저장된 이력서가 없습니다.</p>
            <p className="mt-2 text-sm text-ink-500">첫 이력서를 등록하면 텍스트가 자동으로 추출됩니다.</p>
          </div>
        )}
      </section>
    </div>
  );
}

function DeleteResumeForm({ resumeId, fileName }: { resumeId: number; fileName: string }) {
  const boundAction = removeResume.bind(null, resumeId);
  const [state, action, pending] = useActionState(boundAction, initialResumeActionState);
  return (
    <form action={action} className="shrink-0">
      <input type="hidden" name="resumeId" value={resumeId} />
      <button type="submit" disabled={pending} aria-label={`${fileName} 삭제`} className="inline-flex items-center gap-2 rounded-lg border border-ink-200 px-3 py-2 text-xs font-bold text-ink-500 hover:border-red-300 hover:text-red-600 disabled:opacity-40">
        <Trash2 aria-hidden="true" className="h-3.5 w-3.5" />{pending ? "삭제 중" : "삭제"}
      </button>
      {state.error ? <p role="alert" className="mt-2 max-w-52 text-xs font-semibold text-red-600">{state.error}</p> : null}
    </form>
  );
}
