"use server";

import { revalidatePath } from "next/cache";
import { BackendApiError, saveProfile } from "@/lib/backend-api";
import type { CareerLevel, Profile, ProfileSkill, SkillLevel } from "@/lib/profile";

export type ProfileFormState = {
  error: string | null;
  message: string | null;
};

export const initialProfileFormState: ProfileFormState = { error: null, message: null };

export async function updateProfile(
  _previousState: ProfileFormState,
  formData: FormData,
): Promise<ProfileFormState> {
  const names = formData.getAll("skillName").map((value) => String(value).trim());
  const levels = formData.getAll("skillLevel").map((value) => String(value));
  if (names.length !== levels.length) {
    return { error: "기술 정보를 다시 확인해 주세요.", message: null };
  }

  const skills: ProfileSkill[] = names
    .map((name, index) => ({ name, level: skillLevel(levels[index]) }))
    .filter((skill) => skill.name.length > 0);

  const input: Profile = {
    desiredJob: String(formData.get("desiredJob") ?? "").trim(),
    desiredLocation: String(formData.get("desiredLocation") ?? "").trim(),
    careerLevel: careerLevel(String(formData.get("careerLevel") ?? "")),
    skills,
  };

  try {
    await saveProfile(input);
  } catch (error) {
    return {
      error: error instanceof BackendApiError ? error.message : "프로필을 저장하지 못했습니다.",
      message: null,
    };
  }

  revalidatePath("/profile");
  return { error: null, message: "프로필을 저장했습니다." };
}

function careerLevel(value: string): CareerLevel {
  if (value === "JUNIOR" || value === "EXPERIENCED") return value;
  return "ENTRY";
}

function skillLevel(value: string | undefined): SkillLevel {
  if (value === "INTERMEDIATE" || value === "ADVANCED") return value;
  return "BEGINNER";
}
