export type CareerLevel = "ENTRY" | "JUNIOR" | "EXPERIENCED";
export type SkillLevel = "BEGINNER" | "INTERMEDIATE" | "ADVANCED";

export type ProfileSkill = {
  name: string;
  level: SkillLevel;
};

export type Profile = {
  desiredJob: string;
  careerLevel: CareerLevel;
  desiredLocation: string;
  skills: ProfileSkill[];
};
