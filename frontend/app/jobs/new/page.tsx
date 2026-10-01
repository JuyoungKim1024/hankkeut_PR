import { ArrowLeft } from "lucide-react";
import Link from "next/link";
import { JobImportForm } from "@/components/job-import-form";

export default function NewJobPostingPage() {
  return (
    <main className="mx-auto max-w-5xl px-5 pb-28 pt-8 sm:px-8 lg:px-10 lg:pb-14 lg:pt-12">
      <Link href="/jobs" className="inline-flex items-center gap-2 text-sm font-bold text-ink-500 hover:text-ink-950">
        <ArrowLeft aria-hidden="true" className="h-4 w-4" />
        공고 보관함
      </Link>
      <header className="mt-8">
        <p className="font-mono-label text-[10px] uppercase text-ink-400">Manual import</p>
        <h1 className="mt-2 text-3xl font-black tracking-tightest sm:text-4xl">공고 추가</h1>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-ink-500">
          브라우저에서 확인한 공고의 URL과 본문을 입력하세요. 같은 URL을 다시 저장하면 기존 내용을 갱신합니다.
        </p>
      </header>
      <section className="mt-10 rounded-3xl border border-ink-200 bg-white p-5 sm:p-8">
        <JobImportForm />
      </section>
    </main>
  );
}
