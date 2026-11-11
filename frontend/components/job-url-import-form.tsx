"use client";

import { useActionState } from "react";
import { createJobPostingFromUrl } from "@/app/jobs/actions";

export function JobUrlImportForm() {
  const [state, action, pending] = useActionState(createJobPostingFromUrl, { error: null });

  return (
    <form action={action} className="space-y-4">
      <label className="block">
        <span className="text-sm font-bold text-ink-700">채용공고 URL</span>
        <div className="mt-2 flex flex-col gap-3 sm:flex-row">
          <input
            className="min-w-0 flex-1 rounded-xl border border-ink-200 bg-white px-4 py-3 text-sm outline-none transition placeholder:text-ink-300 focus:border-ink-950 focus:ring-2 focus:ring-ink-950/10"
            name="originalUrl"
            type="url"
            required
            placeholder="https://..."
            disabled={pending}
          />
          <button
            type="submit"
            disabled={pending}
            className="rounded-xl bg-lime-300 px-5 py-3 text-sm font-black text-ink-950 transition hover:bg-lime-200 disabled:cursor-not-allowed disabled:opacity-50"
          >
            {pending ? "가져오는 중..." : "URL로 가져오기"}
          </button>
        </div>
      </label>

      {state.error ? (
        <p role="alert" aria-live="polite" className="rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">
          {state.error}
        </p>
      ) : null}
      <p className="text-xs leading-5 text-ink-500">
        현재 잡코리아 공개 공고 URL을 지원합니다. 같은 공고는 6시간 동안 다시 요청하지 않으며 로그인, CAPTCHA 또는 접근 제한이 있는 페이지는 가져오지 않습니다.
      </p>
    </form>
  );
}
