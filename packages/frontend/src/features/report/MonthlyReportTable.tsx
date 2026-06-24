"use client";

import { type Column, DataTable } from "@/components/DataTable";
import { formatMinutes } from "@/features/attendance/format";
import type { MonthlyRecordResponse } from "./report-api";

const columns: Column<MonthlyRecordResponse>[] = [
  { key: "employeeName", header: "社員名" },
  { key: "departmentName", header: "部署" },
  {
    key: "workDays",
    header: "出勤日数",
    render: (record) => `${record.workDays}日`,
  },
  {
    key: "totalWorkMinutes",
    header: "総労働時間",
    render: (record) => formatMinutes(record.totalWorkMinutes),
  },
  {
    key: "totalOvertimeMinutes",
    header: "残業時間",
    render: (record) => formatMinutes(record.totalOvertimeMinutes),
  },
  {
    key: "absentDays",
    header: "欠勤日数",
    render: (record) => `${record.absentDays}日`,
  },
];

interface MonthlyReportTableProps {
  records: MonthlyRecordResponse[];
}

export function MonthlyReportTable({ records }: MonthlyReportTableProps) {
  return (
    <DataTable<MonthlyRecordResponse & Record<string, unknown>>
      columns={columns as Column<MonthlyRecordResponse & Record<string, unknown>>[]}
      data={records as (MonthlyRecordResponse & Record<string, unknown>)[]}
      rowKey={(item) => item.employeeId}
      emptyMessage="該当するデータがありません"
    />
  );
}
