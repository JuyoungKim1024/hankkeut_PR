export type JobRecommendation = {
  id: number;
  company: string;
  title: string;
  location: string;
  career: string;
  deadline: string;
  score: number;
  matchedSkills: string[];
  missingSkills: string[];
  isNew?: boolean;
};

export const recommendations: JobRecommendation[] = [
  {
    id: 1,
    company: "모노랩스",
    title: "백엔드 엔지니어",
    location: "서울 강남구",
    career: "신입 · 경력 2년 이하",
    deadline: "D-8",
    score: 86,
    matchedSkills: ["Java", "Spring Boot", "MySQL"],
    missingSkills: ["Kafka"],
    isNew: true,
  },
  {
    id: 2,
    company: "플로우데이터",
    title: "플랫폼 서버 개발자",
    location: "서울 성동구",
    career: "신입",
    deadline: "D-12",
    score: 79,
    matchedSkills: ["Java", "JPA", "Docker"],
    missingSkills: ["AWS"],
  },
  {
    id: 3,
    company: "프레임웍스",
    title: "주니어 백엔드 개발자",
    location: "경기 성남시",
    career: "신입 · 경력 1년 이하",
    deadline: "D-4",
    score: 74,
    matchedSkills: ["Spring Boot", "REST API"],
    missingSkills: ["Redis", "Kubernetes"],
  },
];

export const preparationSteps = [
  { label: "기본 정보", detail: "희망 조건 등록 완료", completed: true },
  { label: "이력서", detail: "최근 업데이트 12일 전", completed: true },
  { label: "기술 스택", detail: "숙련도를 추가해 주세요", completed: false },
];

export const missingSkillSummary = [
  { name: "Kafka", count: 8, level: "먼저 학습" },
  { name: "AWS", count: 6, level: "보완 추천" },
  { name: "Redis", count: 5, level: "보완 추천" },
];
