"use client";

import { CorrectionList } from "@/features/correction/CorrectionList";

export default function CorrectionsPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">修正申請</h1>
      <CorrectionList />
    </div>
  );
}
