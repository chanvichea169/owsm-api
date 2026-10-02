package com.example.attendanceService.repository;

import com.example.attendanceService.model.Employee;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    Optional<Employee> findByEmail(String email);

    Optional<Employee> findByUsername(String username);

    Optional<Employee> findByAttendanceCode(String attendanceCode);

    boolean existsByAttendanceCode(String attendanceCode);

    List<Employee> findByCompanyId(Long companyId);

    @Modifying
    @Query("delete from Employee employee where employee.company.id = :companyId")
    void deleteAllByCompanyId(@Param("companyId") Long companyId);

    /** Keeps employees but clears the office link before the office is deleted. */
    @Modifying
    @Query("update Employee employee set employee.office = null where employee.office.id = :officeId")
    int detachOffice(@Param("officeId") Long officeId);
}
