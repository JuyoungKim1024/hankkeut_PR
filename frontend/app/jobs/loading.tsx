export default function JobPostingsLoading() {
  return (
    <main className="mx-auto max-w-[1320px] animate-pulse px-5 pb-28 pt-8 sm:px-8 lg:px-10 lg:pt-12">
      <div className="h-10 w-52 rounded-xl bg-ink-200" />
      <div className="mt-4 h-5 w-80 max-w-full rounded bg-ink-100" />
      <div className="mt-10 h-20 rounded-2xl bg-white" />
      <div className="mt-8 grid gap-4 xl:grid-cols-2">
        {Array.from({ length: 4 }, (_, index) => <div key={index} className="h-52 rounded-2xl bg-white" />)}
      </div>
    </main>
  );
}
