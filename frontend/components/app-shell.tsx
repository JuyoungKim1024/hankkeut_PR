import {
  Bookmark,
  BriefcaseBusiness,
  FileText,
  Home,
  Menu,
  UserRound,
} from "lucide-react";
import type { ReactNode } from "react";

const navigation = [
  { label: "홈", icon: Home, active: true },
  { label: "채용공고", icon: BriefcaseBusiness },
  { label: "지원 관리", icon: Bookmark },
  { label: "준비 노트", icon: FileText },
  { label: "내 프로필", icon: UserRound },
];

export function AppShell({ children }: { children: ReactNode }) {
  return (
    <div className="min-h-screen lg:grid lg:grid-cols-[248px_minmax(0,1fr)]">
      <aside className="hidden border-r border-ink-200 bg-white lg:flex lg:min-h-screen lg:flex-col lg:px-5 lg:py-6">
        <Logo />

        <nav aria-label="주요 메뉴" className="mt-12 space-y-1">
          {navigation.map(({ label, icon: Icon, active }) => (
            <a
              key={label}
              href="#"
              aria-current={active ? "page" : undefined}
              className={`flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold transition-colors ${
                active ? "bg-ink-950 text-white" : "text-ink-500 hover:bg-ink-100 hover:text-ink-950"
              }`}
            >
              <Icon aria-hidden="true" className="h-[18px] w-[18px]" strokeWidth={2} />
              {label}
            </a>
          ))}
        </nav>

        <div className="mt-auto rounded-2xl bg-ink-950 p-4 text-white">
          <p className="font-mono-label text-[10px] uppercase text-ink-400">Profile check</p>
          <p className="mt-3 text-sm font-bold">프로필을 완성하면</p>
          <p className="mt-1 text-xs leading-5 text-ink-300">추천 정확도를 더 높일 수 있어요.</p>
          <a href="#" className="mt-4 inline-flex text-xs font-bold text-accent">
            80%에서 이어서 하기 →
          </a>
        </div>
      </aside>

      <div className="min-w-0">
        <header className="sticky top-0 z-40 flex h-16 items-center justify-between border-b border-ink-200 bg-white/95 px-5 backdrop-blur sm:px-8 lg:px-10">
          <div className="lg:hidden">
            <Logo />
          </div>
          <p className="hidden text-sm font-medium text-ink-500 lg:block">내게 맞는 기회를 한 끗 더 가까이</p>
          <div className="flex items-center gap-3">
            <button
              type="button"
              className="hidden rounded-lg border border-ink-200 px-3 py-2 text-xs font-bold text-ink-600 hover:border-ink-950 sm:block"
            >
              이력서 업데이트
            </button>
            <button
              type="button"
              aria-label="메뉴 열기"
              className="grid h-9 w-9 place-items-center rounded-full bg-ink-950 text-xs font-extrabold text-white lg:hidden"
            >
              <Menu aria-hidden="true" className="h-4 w-4" />
            </button>
            <button
              type="button"
              aria-label="내 프로필"
              className="hidden h-9 w-9 place-items-center rounded-full bg-ink-950 text-xs font-extrabold text-white lg:grid"
            >
              한
            </button>
          </div>
        </header>

        {children}

        <nav
          aria-label="모바일 주요 메뉴"
          className="fixed inset-x-0 bottom-0 z-50 grid grid-cols-4 border-t border-ink-200 bg-white px-2 pb-[max(0.5rem,env(safe-area-inset-bottom))] pt-2 lg:hidden"
        >
          {navigation.slice(0, 4).map(({ label, icon: Icon, active }) => (
            <a
              key={label}
              href="#"
              aria-current={active ? "page" : undefined}
              className={`flex flex-col items-center gap-1 text-[10px] font-semibold ${active ? "text-ink-950" : "text-ink-400"}`}
            >
              <Icon aria-hidden="true" className="h-5 w-5" strokeWidth={active ? 2.4 : 1.8} />
              {label}
            </a>
          ))}
        </nav>
      </div>
    </div>
  );
}

function Logo() {
  return (
    <a href="#" className="inline-flex items-center gap-2" aria-label="한끗 홈">
      <span className="grid h-8 w-8 place-items-center rounded-lg bg-accent text-sm font-black text-ink-950">ㅎ</span>
      <span className="text-lg font-black tracking-tightest">한끗</span>
    </a>
  );
}
