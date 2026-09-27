package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.District;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistrictRepository extends JpaRepository<District, Integer> {
    List<District> findByProvinceProvinceCode(Integer provinceCode);

    long countByProvinceProvinceCode(Integer provinceCode);
}
