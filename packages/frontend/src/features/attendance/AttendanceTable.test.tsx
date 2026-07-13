import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, it } from "vitest";
import { AttendanceTable } from "./AttendanceTable";
import type { DailyAttendanceResponse } from "./attendance-api";

describe("AttendanceTable — メモ列表示", () => {
  afterEach(() => cleanup());
  const mockDays: DailyAttendanceResponse[] = [
    {
      date: "2025-01-15",
      records: [
        {
          id: "rec-1",
          workDate: "2025-01-15",
          clockIn: "2025-01-14T23:00:00Z",
          clockOut: "2025-01-15T08:00:00Z",
          corrected: false,
          clockInMemo: "直行",
          clockOutMemo: "直帰",
        },
      ],
      totalWorkMinutes: 540,
      breakMinutes: 60,
      workMinutes: 480,
      overtimeMinutes: 0,
    },
  ];

  it("備考列のヘッダーが表示される", () => {
    render(<AttendanceTable days={mockDays} />);

    expect(screen.getByText("備考")).toBeInTheDocument();
  });

  it("出勤メモと退勤メモが表示される", () => {
    render(<AttendanceTable days={mockDays} />);

    expect(screen.getByText("直行")).toBeInTheDocument();
    expect(screen.getByText("直帰")).toBeInTheDocument();
  });

  it("メモがない場合は空欄", () => {
    const daysNoMemo: DailyAttendanceResponse[] = [
      {
        date: "2025-01-16",
        records: [
          {
            id: "rec-2",
            workDate: "2025-01-16",
            clockIn: "2025-01-15T23:00:00Z",
            clockOut: "2025-01-16T08:00:00Z",
            corrected: false,
            clockInMemo: null,
            clockOutMemo: null,
          },
        ],
        totalWorkMinutes: 540,
        breakMinutes: 60,
        workMinutes: 480,
        overtimeMinutes: 0,
      },
    ];

    render(<AttendanceTable days={daysNoMemo} />);

    expect(screen.queryByText("直行")).not.toBeInTheDocument();
    expect(screen.queryByText("直帰")).not.toBeInTheDocument();
  });
});
