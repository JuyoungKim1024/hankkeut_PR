import { ArrowRight, Check, Circle, Sparkles } from "lucide-react";
import { JobCard } from "@/components/job-card";
import { missingSkillSummary, preparationSteps, recommendations } from "@/lib/dashboard-data";

export function Dashboard() {
  return (
    <main className="mx-auto max-w-[1480px] px-5 pb-28 pt-8 sm:px-8 lg:px-10 lg:pb-14 lg:pt-12">
      <section className="grid gap-5 xl:grid-cols-[minmax(0,1.55fr)_minmax(320px,0.75fr)]">
        <div className="overflow-hidden rounded-3xl bg-ink-950 p-6 text-white sm:p-8 lg:p-10">
          <p className="font-mono-label text-[10px] uppercase text-ink-400">Sunday · Job briefing</p>
          <div className="mt-10 max-w-2xl">
            <h1 className="text-3xl font-black leading-tight tracking-tightest sm:text-4xl lg:text-[44px]">
              민준님에게 맞는
              <br />새 공고가 <span className="text-accent">12개</span> 도착했어요.
            </h1>
            <p className="mt-4 max-w-lg text-sm leading-6 text-ink-300 sm:text-base">
              등록한 백엔드 경험과 희망 조건을 기준으로 분석했어요. 가장 잘 맞는 공고부터 준비해 보세요.
            </p>
          </div>
          <div className="mt-10 flex flex-wrap gap-3">
            <a href="#recommendations" className="inline-flex items-center gap-2 rounded-xl bg-accent px-4 py-3 text-sm font-extrabold text-ink-950">
              추천 공고 확인
              <ArrowRight aria-hidden="true" className="h-4 w-4" />
            </a>
            <a href="#" className="rounded-xl border border-ink-700 px-4 py-3 text-sm font-bold text-white hover:border-ink-400">
              추천 조건 수정
            </a>
          </div>
        </div>

        <aside className="card-shadow rounded-3xl border border-ink-200 bg-white p-6 sm:p-8">
          <div className="flex items-start justify-between">
            <div>
              <p className="font-mono-label text-[10px] uppercase text-ink-400">Ready to apply</p>
              <h2 className="mt-2 text-xl font-extrabold tracking-tight">지원 준비도</h2>
            </div>
            <span className="text-3xl font-black tracking-tightest">80%</span>
          </div>
          <div className="mt-5 h-1.5 overflow-hidden rounded-full bg-ink-100">
            <div className="h-full w-4/5 rounded-full bg-ink-950" />
          </div>
          <ol className="mt-7 space-y-5">
            {preparationSteps.map((step) => (
              <li key={step.label} className="flex gap-3">
                <span className={`mt-0.5 grid h-5 w-5 shrink-0 place-items-center rounded-full ${step.completed ? "bg-ink-950 text-white" : "border border-ink-300 text-ink-300"}`}>
                  {step.completed ? <Check aria-hidden="true" className="h-3 w-3" /> : <Circle aria-hidden="true" className="h-2 w-2" />}
                </span>
                <div>
                  <p className="text-sm font-bold">{step.label}</p>
                  <p className="mt-0.5 text-xs text-ink-500">{step.detail}</p>
                </div>
              </li>
            ))}
          </ol>
        </aside>
      </section>

      <section id="recommendations" className="mt-12 scroll-mt-24">
        <SectionTitle eyebrow="For you" title="먼저 볼 추천 공고" action="전체 공고 보기" />
        <div className="mt-5 grid gap-4 xl:grid-cols-3">
          {recommendations.map((job) => (
            <JobCard key={job.id} job={job} />
          ))}
        </div>
      </section>

      <section className="mt-12 grid gap-5 xl:grid-cols-[minmax(0,1.15fr)_minmax(300px,0.85fr)]">
        <div className="rounded-3xl border border-ink-200 bg-white p-6 sm:p-8">
          <SectionTitle eyebrow="Skill gap" title="추천 공고에서 자주 부족한 기술" action="학습 노트 보기" />
          <div className="mt-7 space-y-5">
            {missingSkillSummary.map((skill, index) => (
              <div key={skill.name} className="grid grid-cols-[32px_minmax(0,1fr)_auto] items-center gap-3">
                <span className="font-mono-label text-xs text-ink-400">0{index + 1}</span>
                <div>
                  <div className="flex items-center justify-between gap-3">
                    <p className="text-sm font-extrabold">{skill.name}</p>
                    <p className="text-[11px] text-ink-500">{skill.count}개 공고</p>
                  </div>
                  <div className="mt-2 h-1 overflow-hidden rounded-full bg-ink-100">
                    <div className="h-full bg-ink-950" style={{ width: `${92 - index * 18}%` }} />
                  </div>
                </div>
                <span className={`ml-2 rounded-full px-2.5 py-1 text-[10px] font-bold ${index === 0 ? "bg-accent text-ink-950" : "bg-ink-100 text-ink-600"}`}>
                  {skill.level}
                </span>
              </div>
            ))}
          </div>
        </div>

        <div className="relative overflow-hidden rounded-3xl bg-accent p-6 sm:p-8">
          <Sparkles aria-hidden="true" className="absolute right-6 top-6 h-6 w-6 text-ink-950" />
          <p className="font-mono-label text-[10px] uppercase text-ink-600">Next action</p>
          <h2 className="mt-8 max-w-xs text-2xl font-black leading-tight tracking-tightest sm:text-3xl">
            Kafka는 핵심 개념부터 20분이면 시작할 수 있어요.
          </h2>
          <p className="mt-4 max-w-sm text-sm leading-6 text-ink-700">
            지원하려는 공고에서 실제로 묻는 범위만 추려 학습 순서를 준비했어요.
          </p>
          <a href="#" className="mt-8 inline-flex items-center gap-2 rounded-xl bg-ink-950 px-4 py-3 text-sm font-extrabold text-white">
            맞춤 학습 시작
            <ArrowRight aria-hidden="true" className="h-4 w-4" />
          </a>
        </div>
      </section>
    </main>
  );
}

function SectionTitle({ eyebrow, title, action }: { eyebrow: string; title: string; action: string }) {
  return (
    <div className="flex items-end justify-between gap-4">
      <div>
        <p className="font-mono-label text-[10px] uppercase text-ink-400">{eyebrow}</p>
        <h2 className="mt-2 text-xl font-black tracking-tight sm:text-2xl">{title}</h2>
      </div>
      <a href="#" className="hidden items-center gap-1 text-xs font-bold text-ink-500 hover:text-ink-950 sm:inline-flex">
        {action}
        <ArrowRight aria-hidden="true" className="h-3.5 w-3.5" />
      </a>
    </div>
  );
}
