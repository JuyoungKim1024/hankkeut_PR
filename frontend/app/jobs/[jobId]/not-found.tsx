import Link from "next/link";

export default function JobPostingNotFound() {
  return (
    <main className="mx-auto grid min-h-[70vh] max-w-2xl place-items-center px-5 text-center">
      <div>
        <p className="font-mono-label text-[10px] uppercase text-ink-400">Not found</p>
        <h1 className="mt-3 text-2xl font-black">공고를 찾을 수 없습니다.</h1>
        <p className="mt-3 text-sm text-ink-500">삭제됐거나 잘못된 주소일 수 있습니다.</p>
        <Link href="/jobs" className="mt-6 inline-flex rounded-xl bg-ink-950 px-5 py-3 text-sm font-bold text-white">공고 보관함으로</Link>
      </div>
    </main>
  );
}
