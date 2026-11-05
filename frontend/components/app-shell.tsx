import Link from "next/link";
import type { ReactNode } from "react";
import { Navigation } from "@/components/navigation";

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[248px_minmax(0,1fr)]">
      <aside className="hidden border-r border-ink-200 bg-white lg:flex lg:min-h-screen lg:flex-col lg:px-5 lg:py-6">
        <Logo />

        <Navigation />

        <div className="mt-auto rounded-2xl border border-ink-200 bg-ink-50 p-4">
          <p className="font-mono-label text-[10px] uppercase text-ink-400">Local workspace</p>
          <p className="mt-3 text-sm font-bold">내 컴퓨터에만 저장</p>
          <p className="mt-1 text-xs leading-5 text-ink-500">계정 없이 공고와 준비 기록을 관리합니다.</p>
        </div>
      </aside>

      <div className="min-w-0">
        <header className="sticky top-0 z-40 flex h-16 items-center justify-between border-b border-ink-200 bg-white/95 px-5 backdrop-blur sm:px-8 lg:px-10">
          <div className="lg:hidden">
            <Logo />
          </div>
          <p className="hidden text-sm font-medium text-ink-500 lg:block">지원할 공고를 한곳에서 정리하세요.</p>
          <span className="rounded-full bg-accent px-3 py-1.5 font-mono-label text-[10px] font-bold uppercase text-ink-950">
            Local only
          </span>
        </header>

        {children}

        <Navigation mobile />
      </div>
    </div>
  );
}

function Logo() {
  return (
    <Link href="/jobs" className="inline-flex items-center gap-2" aria-label="한끗 공고 보관함">
      <span className="grid h-8 w-8 place-items-center rounded-lg bg-accent text-sm font-black text-ink-950">ㅎ</span>
      <span className="text-lg font-black tracking-tightest">한끗</span>
    </Link>
  );
}
