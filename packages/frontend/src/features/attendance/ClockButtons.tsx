"use client";

import { LogIn, LogOut } from "lucide-react";
import { useRef } from "react";
import { Skeleton } from "@/components/ui/skeleton";
import { CurrentTime } from "./CurrentTime";
import { formatTime } from "./format";
import { useClockIn, useClockOut, useTodayStatus } from "./useAttendance";

const BUTTON_COLORS = {
  blue: "bg-blue-500 hover:bg-blue-600 active:bg-blue-700",
  orange: "bg-orange-500 hover:bg-orange-600 active:bg-orange-700",
} as const;

interface ClockButtonProps {
  disabled: boolean;
  onClick: () => void;
  icon: React.ReactNode;
  label: string;
  color: keyof typeof BUTTON_COLORS;
}

function ClockButton({ disabled, onClick, icon, label, color }: ClockButtonProps) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className={`flex flex-col items-center justify-center gap-2 rounded-xl py-8 text-white transition-colors disabled:bg-gray-200 disabled:text-gray-400 ${BUTTON_COLORS[color]}`}
    >
      {icon}
      <span className="text-lg font-bold">{label}</span>
    </button>
  );
}

const STATUS_LABELS = {
  NOT_CLOCKED_IN: "未出勤",
  CLOCKED_IN: "勤務中",
  CLOCKED_OUT: "退勤済み",
} as const;

export function ClockButtons() {
  const { data: todayStatus, isLoading } = useTodayStatus();
  const clockInMutation = useClockIn();
  const clockOutMutation = useClockOut();
  const memoRef = useRef<HTMLInputElement>(null);

  if (isLoading) {
    return (
      <div className="rounded-lg border p-6 space-y-4">
        <Skeleton className="h-12 w-48 mx-auto" />
        <Skeleton className="h-5 w-24 mx-auto" />
        <div className="flex justify-center gap-4">
          <Skeleton className="h-10 w-28" />
          <Skeleton className="h-10 w-28" />
        </div>
      </div>
    );
  }

  const status = todayStatus?.status ?? "NOT_CLOCKED_IN";
  const canClockIn = status === "NOT_CLOCKED_IN" || status === "CLOCKED_OUT";
  const canClockOut = status === "CLOCKED_IN";
  const isPending = clockInMutation.isPending || clockOutMutation.isPending;

  const getMemo = () => memoRef.current?.value || undefined;

  const lastRecord = todayStatus?.records[todayStatus.records.length - 1];

  return (
    <div className="rounded-lg border p-6 space-y-4">
      <CurrentTime />
      <div className="flex items-center justify-center gap-2">
        <span className="text-sm text-muted-foreground">{STATUS_LABELS[status]}</span>
        {lastRecord && status === "CLOCKED_IN" && (
          <span className="text-sm text-muted-foreground">
            ({formatTime(lastRecord.clockIn)} ~)
          </span>
        )}
      </div>
      <div className="max-w-md mx-auto">
        <input
          ref={memoRef}
          type="text"
          placeholder="メモ（任意・100文字以内）"
          maxLength={100}
          className="w-full rounded-lg border px-3 py-2 text-sm"
        />
      </div>
      <div className="grid grid-cols-2 gap-4 max-w-md mx-auto">
        <ClockButton
          disabled={!canClockIn || isPending}
          onClick={() => clockInMutation.mutate(getMemo())}
          icon={<LogIn className="h-8 w-8" />}
          label="出勤"
          color="blue"
        />
        <ClockButton
          disabled={!canClockOut || isPending}
          onClick={() => clockOutMutation.mutate(getMemo())}
          icon={<LogOut className="h-8 w-8" />}
          label="退勤"
          color="orange"
        />
      </div>
    </div>
  );
}
