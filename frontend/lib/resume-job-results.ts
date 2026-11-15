export type ResumeJobResults = {
  detectedSkills: string[];
  items: {
    title: string;
    location: string;
    url: string;
    score: number;
    matchedSkills: string[];
    missingSkills: string[];
  }[];
};
