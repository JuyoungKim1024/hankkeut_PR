export type ResumeFileType = "PDF" | "DOCX";

export type ResumeSummary = {
  id: number;
  originalFileName: string;
  fileType: ResumeFileType;
  extractedTextLength: number;
  createdAt: string;
};
