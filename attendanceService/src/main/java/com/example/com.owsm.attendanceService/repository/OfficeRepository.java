package com.example.attendanceService.repository;

import com.example.attendanceService.model.Office;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OfficeRepository extends JpaRepository<Office, Long> {

    List<Office> findByCompanyId(Long companyId);

    Optional<Office> findByIdAndCompanyId(Long id, Long companyId);

    @Modifying
    @Query("delete from Office office where office.company.id = :companyId")
    void deleteAllByCompanyId(@Param("companyId") Long companyId);
}
