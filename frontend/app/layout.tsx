import type { Metadata } from "next";
import type { ReactNode } from "react";
import "./globals.css";

export const metadata: Metadata = {
  title: "한끗 | 나에게 맞는 채용 준비",
  description: "채용공고 분석부터 부족 기술 학습과 지원 준비까지 한 번에 연결합니다.",
};

export default function RootLayout({ children }: Readonly<{ children: ReactNode }>) {
  return (
    <html lang="ko">
      <body>{children}</body>
    </html>
  );
}
