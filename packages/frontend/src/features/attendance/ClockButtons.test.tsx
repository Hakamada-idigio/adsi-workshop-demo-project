import { cleanup, render, screen } from "@testing-library/react";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ClockButtons } from "./ClockButtons";

vi.mock("./useAttendance", () => ({
  useTodayStatus: vi.fn(),
  useClockIn: vi.fn(() => ({ mutate: vi.fn(), isPending: false })),
  useClockOut: vi.fn(() => ({ mutate: vi.fn(), isPending: false })),
}));

import { useTodayStatus } from "./useAttendance";
const mockUseTodayStatus = vi.mocked(useTodayStatus);

describe("ClockButtons", () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    cleanup();
    vi.useRealTimers();
  });

  it("未出勤時は出勤ボタンが有効、退勤ボタンが無効", () => {
    mockUseTodayStatus.mockReturnValue({
      data: { status: "NOT_CLOCKED_IN", records: [] },
      isLoading: false,
    } as ReturnType<typeof useTodayStatus>);

    render(<ClockButtons />);

    const buttons = screen.getAllByRole("button");
    const clockInBtn = buttons.find((b) => b.textContent === "出勤")!;
    const clockOutBtn = buttons.find((b) => b.textContent === "退勤")!;

    expect(clockInBtn).not.toBeDisabled();
    expect(clockOutBtn).toBeDisabled();
  });

  it("勤務中は出勤ボタンが無効、退勤ボタンが有効", () => {
    mockUseTodayStatus.mockReturnValue({
      data: {
        status: "CLOCKED_IN",
        records: [{ clockIn: "2025-01-15T00:00:00Z", clockOut: null }],
      },
      isLoading: false,
    } as ReturnType<typeof useTodayStatus>);

    render(<ClockButtons />);

    const buttons = screen.getAllByRole("button");
    const clockInBtn = buttons.find((b) => b.textContent === "出勤")!;
    const clockOutBtn = buttons.find((b) => b.textContent === "退勤")!;

    expect(clockInBtn).toBeDisabled();
    expect(clockOutBtn).not.toBeDisabled();
  });

  it("退勤済みは出勤ボタンが有効（再出勤可）、退勤ボタンが無効", () => {
    mockUseTodayStatus.mockReturnValue({
      data: {
        status: "CLOCKED_OUT",
        records: [
          { clockIn: "2025-01-14T23:00:00Z", clockOut: "2025-01-15T08:00:00Z" },
        ],
      },
      isLoading: false,
    } as ReturnType<typeof useTodayStatus>);

    render(<ClockButtons />);

    const buttons = screen.getAllByRole("button");
    const clockInBtn = buttons.find((b) => b.textContent === "出勤")!;
    const clockOutBtn = buttons.find((b) => b.textContent === "退勤")!;

    expect(clockInBtn).not.toBeDisabled();
    expect(clockOutBtn).toBeDisabled();
  });
});
