import { cleanup, render, screen } from "@testing-library/react";
import { createElement } from "react";
import { type Mock, afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ClockButtons } from "./ClockButtons";
import { useClockIn, useClockOut, useTodayStatus } from "./useAttendance";

vi.mock("./useAttendance", () => ({
  useTodayStatus: vi.fn(),
  useClockIn: vi.fn(),
  useClockOut: vi.fn(),
}));

vi.mock("@/components/ui/skeleton", () => ({
  Skeleton: (props: Record<string, unknown>) =>
    createElement("div", { "data-testid": "skeleton", ...props }),
}));

const mockClockInMutate = vi.fn();
const mockClockOutMutate = vi.fn();

function setupMocks(status: "NOT_CLOCKED_IN" | "CLOCKED_IN" | "CLOCKED_OUT") {
  (useTodayStatus as Mock).mockReturnValue({
    data: {
      status,
      records:
        status === "NOT_CLOCKED_IN"
          ? []
          : [{ clockIn: "2025-01-15T00:00:00Z", clockOut: null }],
    },
    isLoading: false,
  });
  (useClockIn as Mock).mockReturnValue({
    mutate: mockClockInMutate,
    isPending: false,
  });
  (useClockOut as Mock).mockReturnValue({
    mutate: mockClockOutMutate,
    isPending: false,
  });
}

describe("ClockButtons", () => {
  afterEach(() => {
    cleanup();
  });

  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("CLOCKED_IN 状態では出勤ボタンが disabled になる", () => {
    setupMocks("CLOCKED_IN");
    render(<ClockButtons />);

    const clockInButton = screen.getByRole("button", { name: "出勤" });
    expect(clockInButton).toBeDisabled();
  });

  it("NOT_CLOCKED_IN 状態では出勤ボタンが enabled になる", () => {
    setupMocks("NOT_CLOCKED_IN");
    render(<ClockButtons />);

    const clockInButton = screen.getByRole("button", { name: "出勤" });
    expect(clockInButton).toBeEnabled();
  });

  it("CLOCKED_IN 状態では退勤ボタンが enabled になる", () => {
    setupMocks("CLOCKED_IN");
    render(<ClockButtons />);

    const clockOutButton = screen.getByRole("button", { name: "退勤" });
    expect(clockOutButton).toBeEnabled();
  });
});
