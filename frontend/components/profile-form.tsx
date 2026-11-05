"use client";

import { Plus, Trash2 } from "lucide-react";
import { useActionState, useState } from "react";
import { initialProfileFormState, updateProfile } from "@/app/profile/actions";
import type { Profile, ProfileSkill } from "@/lib/profile";

const inputClass = "mt-2 h-11 w-full rounded-xl border border-ink-200 bg-white px-3 text-sm outline-none transition focus:border-ink-950 focus:ring-2 focus:ring-ink-950/10";

export function ProfileForm({ profile }: { profile: Profile | null }) {
  const [state, action, pending] = useActionState(updateProfile, initialProfileFormState);
  const [skills, setSkills] = useState<ProfileSkill[]>(profile?.skills ?? []);

  function addSkill() {
    if (skills.length >= 50) return;
    setSkills((current) => [...current, { name: "", level: "BEGINNER" }]);
  }

  function removeSkill(index: number) {
    setSkills((current) => current.filter((_, itemIndex) => itemIndex !== index));
  }

  return (
    <form action={action} className="space-y-9">
      <fieldset disabled={pending} className="space-y-9 disabled:opacity-70">
        <div className="grid gap-5 sm:grid-cols-2">
          <Field label="희망 직무" name="desiredJob" required defaultValue={profile?.desiredJob} placeholder="백엔드 개발자" />
          <Field label="희망 지역" name="desiredLocation" required defaultValue={profile?.desiredLocation} placeholder="서울" />
          <label className="sm:col-span-2">
            <span className="text-sm font-bold text-ink-700">경력 수준</span>
            <select name="careerLevel" defaultValue={profile?.careerLevel ?? "ENTRY"} className={inputClass}>
              <option value="ENTRY">신입</option>
              <option value="JUNIOR">주니어</option>
              <option value="EXPERIENCED">경력</option>
            </select>
          </label>
        </div>

        <section>
          <div className="flex items-center justify-between gap-4">
            <div>
              <h2 className="text-lg font-black">기술 스택</h2>
              <p className="mt-1 text-xs text-ink-500">실제로 사용해 본 기술과 현재 숙련도를 입력하세요.</p>
            </div>
            <button type="button" onClick={addSkill} disabled={skills.length >= 50} className="inline-flex items-center gap-1.5 rounded-lg border border-ink-200 px-3 py-2 text-xs font-bold hover:border-ink-950 disabled:opacity-40">
              <Plus aria-hidden="true" className="h-3.5 w-3.5" />기술 추가
            </button>
          </div>

          {skills.length > 0 ? (
            <div className="mt-5 space-y-3">
              {skills.map((skill, index) => (
                <div key={index} className="grid grid-cols-[minmax(0,1fr)_140px_40px] gap-2">
                  <label>
                    <span className="sr-only">기술 이름 {index + 1}</span>
                    <input name="skillName" defaultValue={skill.name} required placeholder="Java" className="h-11 w-full rounded-xl border border-ink-200 px-3 text-sm outline-none focus:border-ink-950" />
                  </label>
                  <label>
                    <span className="sr-only">숙련도 {index + 1}</span>
                    <select name="skillLevel" defaultValue={skill.level} className="h-11 w-full rounded-xl border border-ink-200 bg-white px-3 text-sm outline-none focus:border-ink-950">
                      <option value="BEGINNER">입문</option>
                      <option value="INTERMEDIATE">활용 가능</option>
                      <option value="ADVANCED">숙련</option>
                    </select>
                  </label>
                  <button type="button" onClick={() => removeSkill(index)} aria-label={`${index + 1}번째 기술 삭제`} className="grid h-11 w-10 place-items-center rounded-xl border border-ink-200 text-ink-400 hover:border-red-300 hover:text-red-600">
                    <Trash2 aria-hidden="true" className="h-4 w-4" />
                  </button>
                </div>
              ))}
            </div>
          ) : (
            <div className="mt-5 rounded-2xl border border-dashed border-ink-300 px-5 py-8 text-center text-sm text-ink-500">등록한 기술이 없습니다.</div>
          )}
        </section>
      </fieldset>

      <div aria-live="polite">
        {state.error ? <p role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm font-semibold text-red-700">{state.error}</p> : null}
        {state.message ? <p className="rounded-xl bg-lime-50 px-4 py-3 text-sm font-semibold text-lime-800">{state.message}</p> : null}
      </div>

      <div className="flex justify-end">
        <button type="submit" disabled={pending} className="rounded-xl bg-ink-950 px-5 py-3 text-sm font-bold text-white disabled:cursor-not-allowed disabled:opacity-50">
          {pending ? "저장 중..." : "프로필 저장"}
        </button>
      </div>
    </form>
  );
}

function Field({ label, name, required, defaultValue, placeholder }: { label: string; name: string; required?: boolean; defaultValue?: string; placeholder?: string }) {
  return (
    <label>
      <span className="text-sm font-bold text-ink-700">{label}</span>
      <input name={name} required={required} defaultValue={defaultValue} placeholder={placeholder} className={inputClass} />
    </label>
  );
}
