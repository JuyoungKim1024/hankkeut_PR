import { Plus, Search } from "lucide-react";
import Link from "next/link";
import { BackendUnavailable } from "@/components/backend-unavailable";
import { JobListCard } from "@/components/job-list-card";
import { BackendApiError, getJobPostings } from "@/lib/backend-api";
import type { JobPostingPage } from "@/lib/job-postings";
import type { JobPostingSort, JobPostingStatus } from "@/lib/job-postings";

type SearchParams = Promise<Record<string, string | string[] | undefined>>;

export default async function JobPostingsPage({ searchParams }: { searchParams: SearchParams }) {
  const raw = await searchParams;
  const query = value(raw.query);
  const location = value(raw.location);
  const status = postingStatus(value(raw.status));
  const sort = postingSort(value(raw.sort));
  const page = nonNegativeNumber(value(raw.page));

  let result: JobPostingPage;
  try {
    result = await getJobPostings({ query, location, status, sort, page, size: 12 });
  } catch (error) {
    if (error instanceof BackendApiError) return <BackendUnavailable />;
    throw error;
  }

  return (
    <main className="mx-auto max-w-[1320px] px-5 pb-28 pt-8 sm:px-8 lg:px-10 lg:pb-14 lg:pt-12">
      <header className="flex flex-col gap-6 sm:flex-row sm:items-end sm:justify-between">
        <div>
          <p className="font-mono-label text-[10px] uppercase text-ink-400">Job archive</p>
          <h1 className="mt-2 text-3xl font-black tracking-tightest sm:text-4xl">공고 보관함</h1>
          <p className="mt-3 text-sm leading-6 text-ink-500">
            지원을 고민 중인 공고를 직접 저장하고 한곳에서 비교하세요.
          </p>
        </div>
        <Link href="/jobs/new" className="inline-flex items-center justify-center gap-2 rounded-xl bg-ink-950 px-4 py-3 text-sm font-bold text-white">
          <Plus aria-hidden="true" className="h-4 w-4" />
          공고 추가
        </Link>
      </header>

      <form className="mt-10 grid gap-3 rounded-2xl border border-ink-200 bg-white p-4 sm:grid-cols-2 xl:grid-cols-[minmax(260px,1fr)_180px_150px_150px_auto]" method="get">
        <label className="relative sm:col-span-2 xl:col-span-1">
          <span className="sr-only">공고 검색</span>
          <Search aria-hidden="true" className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-ink-400" />
          <input name="query" defaultValue={query} placeholder="회사명, 공고명, 직무 검색" className="h-11 w-full rounded-xl border border-ink-200 pl-10 pr-3 text-sm outline-none focus:border-ink-950" />
        </label>
        <label>
          <span className="sr-only">지역</span>
          <input name="location" defaultValue={location} placeholder="지역" className="h-11 w-full rounded-xl border border-ink-200 px-3 text-sm outline-none focus:border-ink-950" />
        </label>
        <label>
          <span className="sr-only">공고 상태</span>
          <select name="status" defaultValue={status} className="h-11 w-full rounded-xl border border-ink-200 bg-white px-3 text-sm outline-none focus:border-ink-950">
            <option value="ACTIVE">진행 중</option>
            <option value="CLOSED">마감</option>
          </select>
        </label>
        <label>
          <span className="sr-only">정렬</span>
          <select name="sort" defaultValue={sort} className="h-11 w-full rounded-xl border border-ink-200 bg-white px-3 text-sm outline-none focus:border-ink-950">
            <option value="LATEST">최신 등록순</option>
            <option value="DEADLINE">마감 임박순</option>
          </select>
        </label>
        <button className="h-11 rounded-xl bg-ink-950 px-5 text-sm font-bold text-white" type="submit">검색</button>
      </form>

      <div className="mt-8 flex items-center justify-between gap-4">
        <p className="text-sm font-bold text-ink-700">총 {result.totalElements.toLocaleString("ko-KR")}개</p>
        {(query || location) ? (
          <Link href={`/jobs?status=${status}&sort=${sort}`} className="text-xs font-bold text-ink-500 underline underline-offset-4">검색 초기화</Link>
        ) : null}
      </div>

      {result.items.length > 0 ? (
        <div className="mt-4 grid gap-4 xl:grid-cols-2">
          {result.items.map((job) => <JobListCard key={job.id} job={job} />)}
        </div>
      ) : (
        <section className="mt-4 rounded-3xl border border-dashed border-ink-300 bg-white px-6 py-20 text-center">
          <p className="text-lg font-black">조건에 맞는 공고가 없습니다.</p>
          <p className="mt-2 text-sm text-ink-500">검색 조건을 바꾸거나 새 공고를 직접 추가해 보세요.</p>
          <Link href="/jobs/new" className="mt-6 inline-flex rounded-xl bg-accent px-4 py-3 text-sm font-bold text-ink-950">첫 공고 추가하기</Link>
        </section>
      )}

      {result.totalPages > 1 ? (
        <nav aria-label="공고 페이지" className="mt-8 flex items-center justify-center gap-3">
          {result.page > 0 ? <PageLink label="이전" page={result.page - 1} raw={raw} /> : <span className="px-4 py-2 text-sm text-ink-300">이전</span>}
          <span className="font-mono-label text-xs text-ink-500">{result.page + 1} / {result.totalPages}</span>
          {result.page + 1 < result.totalPages ? <PageLink label="다음" page={result.page + 1} raw={raw} /> : <span className="px-4 py-2 text-sm text-ink-300">다음</span>}
        </nav>
      ) : null}
    </main>
  );
}

function PageLink({ label, page, raw }: { label: string; page: number; raw: Record<string, string | string[] | undefined> }) {
  const params = new URLSearchParams();
  for (const [key, entry] of Object.entries(raw)) {
    if (typeof entry === "string" && key !== "page") params.set(key, entry);
  }
  params.set("page", String(page));
  return <Link href={`/jobs?${params.toString()}`} className="rounded-lg border border-ink-200 px-4 py-2 text-sm font-bold hover:border-ink-950">{label}</Link>;
}

function value(input: string | string[] | undefined): string {
  return typeof input === "string" ? input.trim() : "";
}

function postingStatus(input: string): JobPostingStatus {
  return input === "CLOSED" ? "CLOSED" : "ACTIVE";
}

function postingSort(input: string): JobPostingSort {
  return input === "DEADLINE" ? "DEADLINE" : "LATEST";
}

function nonNegativeNumber(input: string): number {
  const parsed = Number.parseInt(input, 10);
  return Number.isInteger(parsed) && parsed >= 0 ? parsed : 0;
}
