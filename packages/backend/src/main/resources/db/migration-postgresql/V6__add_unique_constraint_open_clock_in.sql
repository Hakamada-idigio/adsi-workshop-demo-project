CREATE UNIQUE INDEX uq_attendance_open_clock_in
    ON attendance_records(employee_id, work_date)
    WHERE clock_out IS NULL;
