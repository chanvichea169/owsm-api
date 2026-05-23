package com.example.attendanceService.repository;

import com.example.attendanceService.model.AttendanceRecord;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByEmployeeIdOrderByRecordedAtDesc(Long employeeId);
}
