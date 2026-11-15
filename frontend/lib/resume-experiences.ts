export type ExperienceInput = {
  project: string;
  role: string;
  skills: string;
  outcome: string;
  evidence: string;
};

export type ExperienceWorkspace = {
  source: string;
  experiences: { id: number; content: ExperienceInput }[];
};
