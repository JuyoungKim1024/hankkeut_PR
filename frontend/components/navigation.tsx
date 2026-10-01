"use client";

import { BriefcaseBusiness, Plus } from "lucide-react";
import Link from "next/link";
import { usePathname } from "next/navigation";

const items = [
  { href: "/jobs", label: "공고 보관함", icon: BriefcaseBusiness, exact: true },
  { href: "/jobs/new", label: "공고 추가", icon: Plus, exact: false },
];

export function Navigation({ mobile = false }: { mobile?: boolean }) {
  const pathname = usePathname();

  if (mobile) {
    return (
      <nav
        aria-label="모바일 주요 메뉴"
        className="fixed inset-x-0 bottom-0 z-50 grid grid-cols-2 border-t border-ink-200 bg-white px-4 pb-[max(0.5rem,env(safe-area-inset-bottom))] pt-2 lg:hidden"
      >
        {items.map((item) => {
          const active = item.exact ? pathname === item.href || pathname.startsWith("/jobs/") && pathname !== "/jobs/new" : pathname === item.href;
          const Icon = item.icon;
          return (
            <Link
              key={item.href}
              href={item.href}
              aria-current={active ? "page" : undefined}
              className={`flex flex-col items-center gap-1 py-1 text-[10px] font-semibold ${active ? "text-ink-950" : "text-ink-400"}`}
            >
              <Icon aria-hidden="true" className="h-5 w-5" strokeWidth={active ? 2.4 : 1.8} />
              {item.label}
            </Link>
          );
        })}
      </nav>
    );
  }

  return (
    <nav aria-label="주요 메뉴" className="mt-12 space-y-1">
      {items.map((item) => {
        const active = item.exact ? pathname === item.href || pathname.startsWith("/jobs/") && pathname !== "/jobs/new" : pathname === item.href;
        const Icon = item.icon;
        return (
          <Link
            key={item.href}
            href={item.href}
            aria-current={active ? "page" : undefined}
            className={`flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-semibold transition-colors ${
              active ? "bg-ink-950 text-white" : "text-ink-500 hover:bg-ink-100 hover:text-ink-950"
            }`}
          >
            <Icon aria-hidden="true" className="h-[18px] w-[18px]" strokeWidth={2} />
            {item.label}
          </Link>
        );
      })}
    </nav>
  );
}
