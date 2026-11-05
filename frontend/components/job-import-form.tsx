"use client";

import { useActionState } from "react";
import {
  createJobPosting,
  initialImportJobPostingState,
} from "@/app/jobs/actions";

const inputClass = "mt-2 w-full rounded-xl border border-ink-200 bg-white px-4 py-3 text-sm outline-none transition placeholder:text-ink-300 focus:border-ink-950 focus:ring-2 focus:ring-ink-950/10";

export function JobImportForm() {
  const [state, action, pending] = useActionState(createJobPosting, initialImportJobPostingState);

  return (
    <form action={action} className="space-y-8">
      <fieldset disabled={pending} className="space-y-6 disabled:opacity-70">
        <div className="grid gap-5 sm:grid-cols-2">
          <Field label="공고 URL" name="originalUrl" type="url" required placeholder="https://..." span />
          <Field label="회사명" name="companyName" required placeholder="회사명" />
          <Field label="공고 제목" name="title" required placeholder="백엔드 개발자" />
          <Field label="직무" name="jobCategory" required placeholder="백엔드" />
          <Field label="경력" name="career" placeholder="신입 · 경력 2년 이하" />
          <Field label="지역" name="location" placeholder="서울" />
          <Field label="게시일" name="postedAt" type="datetime-local" />
          <Field label="마감일" name="expiredAt" type="datetime-local" />
        </div>

        <TextArea label="공고 본문" name="description" required rows={10} placeholder="담당 업무와 기술 환경을 포함한 공고 본문을 붙여 넣으세요." />
        <div className="grid gap-5 lg:grid-cols-2">
          <TextArea label="자격요건" name="qualification" rows={6} placeholder="자격요건을 붙여 넣으세요." />
          <TextArea label="우대사항" name="preference" rows={6} placeholder="우대사항을 붙여 넣으세요." />
        </div>
      </fieldset>

      {state.error ? (
        <p role="alert" aria-live="polite" className="rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          {state.error}
        </p>
      ) : null}

      <div className="flex justify-end">
        <button
          type="submit"
          disabled={pending}
          className="rounded-xl bg-ink-950 px-5 py-3 text-sm font-bold text-white transition hover:bg-ink-800 disabled:cursor-not-allowed disabled:opacity-50"
        >
          {pending ? "저장 중..." : "공고 저장"}
        </button>
      </div>
    </form>
  );
}

function Field({
  label,
  name,
  type = "text",
  required = false,
  placeholder,
  span = false,
}: {
  label: string;
  name: string;
  type?: string;
  required?: boolean;
  placeholder?: string;
  span?: boolean;
}) {
  return (
    <label className={span ? "sm:col-span-2" : undefined}>
      <span className="text-sm font-bold text-ink-700">{label}</span>
      <input className={inputClass} id={name} name={name} type={type} required={required} placeholder={placeholder} />
    </label>
  );
}

function TextArea({
  label,
  name,
  required = false,
  rows,
  placeholder,
}: {
  label: string;
  name: string;
  required?: boolean;
  rows: number;
  placeholder?: string;
}) {
  return (
    <label className="block">
      <span className="text-sm font-bold text-ink-700">{label}</span>
      <textarea className={inputClass} id={name} name={name} required={required} rows={rows} placeholder={placeholder} />
    </label>
  );
}
