import Link from "next/link";

export function BackendUnavailable() {
  return (
    <main className="mx-auto grid min-h-[70vh] max-w-2xl place-items-center px-5 text-center">
      <div>
        <p className="font-mono-label text-[10px] uppercase text-ink-400">Backend offline</p>
        <h1 className="mt-3 text-2xl font-black">로컬 서버를 기다리고 있습니다.</h1>
        <p className="mt-3 text-sm leading-6 text-ink-500">백엔드와 MySQL이 실행 중인지 확인한 뒤 다시 시도해 주세요.</p>
        <Link href="/jobs" className="mt-6 inline-flex rounded-xl bg-ink-950 px-5 py-3 text-sm font-bold text-white">다시 시도</Link>
      </div>
    </main>
  );
}
