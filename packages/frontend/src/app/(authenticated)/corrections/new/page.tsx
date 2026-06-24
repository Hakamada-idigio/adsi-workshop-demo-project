"use client";

import { CorrectionForm } from "@/features/correction/CorrectionForm";

export default function NewCorrectionPage() {
  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold">修正申請 - 新規作成</h1>
      <CorrectionForm />
    </div>
  );
}
