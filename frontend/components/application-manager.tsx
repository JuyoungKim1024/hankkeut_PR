"use client";

import { CalendarDays, Trash2 } from "lucide-react";
import Link from "next/link";
import { useActionState } from "react";
import {
  createApplication,
  type ApplicationActionState,
  removeApplication,
  updateApplication,
} from "@/app/applications/actions";
import {
  allowedStatuses,
  applicationStatusLabels,
  type ApplicationRecord,
} from "@/lib/applications";
import type { JobPostingSummary } from "@/lib/job-postings";

const initialApplicationActionState: ApplicationActionState = { error: null, message: null };

const fieldClass = "h-11 w-full rounded-xl border border-ink-200 bg-white px-3 text-sm outline-none focus:border-ink-950";

export function ApplicationManager({ applications, jobs }: { applications: ApplicationRecord[]; jobs: JobPostingSummary[] }) {
  const [state, action, pending] = useActionState(createApplication, initialApplicationActionState);
  const trackedIds = new Set(applications.map((application) => application.jobPostingId));
  const availableJobs = jobs.filter((job) => !trackedIds.has(job.id));

  return (
    <div className="space-y-8">
      <form action={action} className="rounded-3xl border border-ink-200 bg-white p-5 sm:p-7">
        <div className="grid gap-3 lg:grid-cols-[minmax(220px,1fr)_140px_minmax(200px,1fr)_190px_auto]">
          <select name="jobPostingId" required className={fieldClass} defaultValue="">
            <option value="" disabled>공고 선택</option>
            {availableJobs.map((job) => <option key={job.id} value={job.id}>{job.companyName} · {job.title}</option>)}
          </select>
          <select name="status" className={fieldClass} defaultValue="INTERESTED">
            <option value="INTERESTED">관심</option><option value="PREPARING">준비</option><option value="APPLIED">지원</option>
          </select>
          <input name="memo" maxLength={5000} placeholder="간단한 메모" className={fieldClass} />
          <input name="nextActionAt" type="datetime-local" className={fieldClass} />
          <button type="submit" disabled={pending || availableJobs.length === 0} className="rounded-xl bg-ink-950 px-4 text-sm font-bold text-white disabled:opacity-40">{pending ? "추가 중" : "추가"}</button>
        </div>
        {availableJobs.length === 0 ? <p className="mt-3 text-xs text-ink-500">추가할 수 있는 진행 중 공고가 없습니다.</p> : null}
        <ActionMessage state={state} />
      </form>

      {applications.length > 0 ? (
        <div className="grid gap-4 xl:grid-cols-2">
          {applications.map((application) => <ApplicationCard key={application.id} application={application} />)}
        </div>
      ) : (
        <div className="rounded-3xl border border-dashed border-ink-300 bg-white px-6 py-16 text-center">
          <p className="font-bold">관리 중인 지원 공고가 없습니다.</p>
          <p className="mt-2 text-sm text-ink-500">공고를 저장한 뒤 관심 또는 준비 상태로 추가해 보세요.</p>
        </div>
      )}
    </div>
  );
}

function ApplicationCard({ application }: { application: ApplicationRecord }) {
  const update = updateApplication.bind(null, application.id);
  const remove = removeApplication.bind(null, application.id);
  const [updateState, updateAction, updating] = useActionState(update, initialApplicationActionState);
  const [deleteState, deleteAction, deleting] = useActionState(remove, initialApplicationActionState);

  return (
    <article className="rounded-2xl border border-ink-200 bg-white p-5 sm:p-6">
      <div className="flex items-start justify-between gap-4">
        <div className="min-w-0">
          <p className="truncate text-sm font-bold text-ink-500">{application.companyName}</p>
          <Link href={`/jobs/${application.jobPostingId}`} className="mt-1 block text-lg font-black hover:underline">{application.jobTitle}</Link>
          <p className="mt-1 text-xs text-ink-400">{application.location ?? "지역 미정"}</p>
        </div>
        <span className="rounded-full bg-accent px-2.5 py-1 text-xs font-bold">{applicationStatusLabels[application.status]}</span>
      </div>

      <form action={updateAction} className="mt-6 space-y-3 border-t border-ink-100 pt-5">
        <div className="grid gap-3 sm:grid-cols-2">
          <select name="status" defaultValue={application.status} className={fieldClass}>
            {allowedStatuses(application.status).map((status) => <option key={status} value={status}>{applicationStatusLabels[status]}</option>)}
          </select>
          <label className="relative">
            <CalendarDays aria-hidden="true" className="pointer-events-none absolute left-3 top-3.5 h-4 w-4 text-ink-400" />
            <input name="nextActionAt" type="datetime-local" defaultValue={toLocalInput(application.nextActionAt)} className={`${fieldClass} pl-9`} />
          </label>
        </div>
        <textarea name="memo" maxLength={5000} defaultValue={application.memo ?? ""} rows={3} placeholder="다음 행동이나 확인할 내용을 적으세요." className="w-full rounded-xl border border-ink-200 p-3 text-sm outline-none focus:border-ink-950" />
        <div className="flex justify-end">
          <button type="submit" disabled={updating} className="rounded-lg bg-ink-950 px-4 py-2 text-xs font-bold text-white disabled:opacity-40">{updating ? "저장 중" : "변경 저장"}</button>
        </div>
        <ActionMessage state={updateState} />
      </form>
      <form action={deleteAction} className="mt-3">
        <input type="hidden" name="applicationId" value={application.id} />
        <button type="submit" disabled={deleting} className="inline-flex items-center gap-1.5 text-xs font-bold text-ink-400 hover:text-red-600"><Trash2 aria-hidden="true" className="h-3.5 w-3.5" />{deleting ? "삭제 중" : "삭제"}</button>
        <ActionMessage state={deleteState} />
      </form>
    </article>
  );
}

function ActionMessage({ state }: { state: { error: string | null; message: string | null } }) {
  if (!state.error && !state.message) return null;
  return <p role={state.error ? "alert" : undefined} className={`mt-3 text-xs font-semibold ${state.error ? "text-red-600" : "text-lime-700"}`}>{state.error ?? state.message}</p>;
}

function toLocalInput(value: string | null): string {
  return value ? value.slice(0, 16) : "";
}
