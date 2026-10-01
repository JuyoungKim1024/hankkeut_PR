import { BackendUnavailable } from "@/components/backend-unavailable";
import { ProfileForm } from "@/components/profile-form";
import { BackendApiError, getProfile } from "@/lib/backend-api";
import type { Profile } from "@/lib/profile";

export default async function ProfilePage() {
  let profile: Profile | null;
  try {
    profile = await getProfile();
  } catch (error) {
    if (error instanceof BackendApiError) return <BackendUnavailable />;
    throw error;
  }

  return (
    <main className="mx-auto max-w-5xl px-5 pb-28 pt-8 sm:px-8 lg:px-10 lg:pb-14 lg:pt-12">
      <header>
        <p className="font-mono-label text-[10px] uppercase text-ink-400">Local profile</p>
        <h1 className="mt-2 text-3xl font-black tracking-tightest sm:text-4xl">내 프로필</h1>
        <p className="mt-3 max-w-2xl text-sm leading-6 text-ink-500">추천과 분석에 사용할 희망 조건과 실제 기술만 기록하세요. 정보는 이 설치 환경에만 저장됩니다.</p>
      </header>
      <section className="mt-10 rounded-3xl border border-ink-200 bg-white p-5 sm:p-8">
        <ProfileForm profile={profile} />
      </section>
    </main>
  );
}
