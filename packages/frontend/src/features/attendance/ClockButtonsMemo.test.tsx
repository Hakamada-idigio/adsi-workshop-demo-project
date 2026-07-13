import { cleanup, render, screen } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ClockButtons } from "./ClockButtons";

vi.mock("./useAttendance", () => ({
  useTodayStatus: vi.fn(),
  useClockIn: vi.fn(() => ({ mutate: vi.fn(), isPending: false })),
  useClockOut: vi.fn(() => ({ mutate: vi.fn(), isPending: false })),
}));

import { useClockIn, useClockOut, useTodayStatus } from "./useAttendance";
const mockUseTodayStatus = vi.mocked(useTodayStatus);
const mockUseClockIn = vi.mocked(useClockIn);
const mockUseClockOut = vi.mocked(useClockOut);

describe("ClockButtons — メモ入力機能", () => {
  const user = userEvent.setup({ advanceTimers: vi.advanceTimersByTime });

  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    cleanup();
    vi.useRealTimers();
  });

  it("打刻ボタンの上にメモ入力欄が表示される", () => {
    mockUseTodayStatus.mockReturnValue({
      data: { status: "NOT_CLOCKED_IN", records: [] },
      isLoading: false,
    } as ReturnType<typeof useTodayStatus>);

    render(<ClockButtons />);

    const memoInput = screen.getByPlaceholderText("メモ（任意・100文字以内）");
    expect(memoInput).toBeInTheDocument();
  });

  it("出勤ボタン押下時にメモの値がmutationに渡される", async () => {
    const mockMutate = vi.fn();
    mockUseTodayStatus.mockReturnValue({
      data: { status: "NOT_CLOCKED_IN", records: [] },
      isLoading: false,
    } as ReturnType<typeof useTodayStatus>);
    mockUseClockIn.mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    } as unknown as ReturnType<typeof useClockIn>);

    render(<ClockButtons />);

    const memoInput = screen.getByPlaceholderText("メモ（任意・100文字以内）");
    await user.type(memoInput, "直行");

    const buttons = screen.getAllByRole("button");
    const clockInBtn = buttons.find((b) => b.textContent === "出勤")!;
    await user.click(clockInBtn);

    expect(mockMutate).toHaveBeenCalledWith("直行");
  });

  it("退勤ボタン押下時にメモの値がmutationに渡される", async () => {
    const mockMutate = vi.fn();
    mockUseTodayStatus.mockReturnValue({
      data: {
        status: "CLOCKED_IN",
        records: [{ clockIn: "2025-01-15T00:00:00Z", clockOut: null }],
      },
      isLoading: false,
    } as ReturnType<typeof useTodayStatus>);
    mockUseClockOut.mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    } as unknown as ReturnType<typeof useClockOut>);

    render(<ClockButtons />);

    const memoInput = screen.getByPlaceholderText("メモ（任意・100文字以内）");
    await user.type(memoInput, "直帰");

    const buttons = screen.getAllByRole("button");
    const clockOutBtn = buttons.find((b) => b.textContent === "退勤")!;
    await user.click(clockOutBtn);

    expect(mockMutate).toHaveBeenCalledWith("直帰");
  });

  it("メモが空でも打刻できる", async () => {
    const mockMutate = vi.fn();
    mockUseTodayStatus.mockReturnValue({
      data: { status: "NOT_CLOCKED_IN", records: [] },
      isLoading: false,
    } as ReturnType<typeof useTodayStatus>);
    mockUseClockIn.mockReturnValue({
      mutate: mockMutate,
      isPending: false,
    } as unknown as ReturnType<typeof useClockIn>);

    render(<ClockButtons />);

    const buttons = screen.getAllByRole("button");
    const clockInBtn = buttons.find((b) => b.textContent === "出勤")!;
    await user.click(clockInBtn);

    expect(mockMutate).toHaveBeenCalled();
  });

  it("メモ入力欄は100文字までに制限される", () => {
    mockUseTodayStatus.mockReturnValue({
      data: { status: "NOT_CLOCKED_IN", records: [] },
      isLoading: false,
    } as ReturnType<typeof useTodayStatus>);

    render(<ClockButtons />);

    const memoInput = screen.getByPlaceholderText("メモ（任意・100文字以内）");
    expect(memoInput).toHaveAttribute("maxLength", "100");
  });
});
