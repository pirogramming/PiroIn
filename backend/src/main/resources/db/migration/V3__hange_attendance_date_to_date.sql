ALTER TABLE attendance_code
ALTER COLUMN attendance_date TYPE DATE
USING attendance_date::DATE;