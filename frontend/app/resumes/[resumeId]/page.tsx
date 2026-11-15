import Link from "next/link";
import { notFound } from "next/navigation";
import { BackendUnavailable } from "@/components/backend-unavailable";
import { ResumeExperienceEditor } from "@/components/resume-experience-editor";
import { BackendApiError, getResumeExperiences } from "@/lib/backend-api";

export default async function ResumeExperiencePage({ params }: { params: Promise<{ resumeId: string }> }) {
  const id = Number((await params).resumeId);
  if (!Number.isSafeInteger(id) || id < 1) notFound();
  let workspace;
  try {
    workspace = await getResumeExperiences(id);
  } catch (error) {
    if (error instanceof BackendApiError && error.status === 404) notFound();
    if (error instanceof BackendApiError) return <BackendUnavailable />;
    throw error;
  }
  return (
    <main className="mx-auto max-w-5xl space-y-6 px-5 pb-28 pt-8 sm:px-8">
      <Link href="/resumes" className="text-sm font-bold underline">이력서 목록</Link>
      <h1 className="text-3xl font-black">이력서 경험 확인</h1>
      <ResumeExperienceEditor resumeId={id} workspace={workspace} />
    </main>
  );
}
