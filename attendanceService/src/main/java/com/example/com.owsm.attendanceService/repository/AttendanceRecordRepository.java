package com.example.attendanceService.repository;

import com.example.attendanceService.model.AttendanceRecord;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    List<AttendanceRecord> findByEmployeeIdOrderByRecordedAtDesc(Long employeeId);

    Optional<AttendanceRecord> findFirstByEmployeeIdOrderByRecordedAtDesc(Long employeeId);

    @Modifying
    @Query("delete from AttendanceRecord record where record.employee.id = :employeeId")
    void deleteAllByEmployeeId(@Param("employeeId") Long employeeId);

    @Modifying
    @Query("delete from AttendanceRecord record where record.employee.company.id = :companyId")
    void deleteAllByCompanyId(@Param("companyId") Long companyId);

    @Query("""
        select record
        from AttendanceRecord record
        join fetch record.employee employee
        join fetch employee.company
        left join fetch employee.office
        order by record.recordedAt desc
        """)
    List<AttendanceRecord> findAllWithEmployeeOrderByRecordedAtDesc();

    @Query("""
        select record
        from AttendanceRecord record
        join fetch record.employee employee
        join fetch employee.company company
        left join fetch employee.office
        where company.id = :companyId
        order by record.recordedAt desc
        """)
    List<AttendanceRecord> findAllWithEmployeeByCompanyOrderByRecordedAtDesc(Long companyId);
}
