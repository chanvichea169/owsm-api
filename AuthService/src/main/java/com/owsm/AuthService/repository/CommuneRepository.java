package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.Commune;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommuneRepository extends JpaRepository<Commune, Integer> {
    List<Commune> findByDistrictDistrictCode(Integer districtCode);

    long countByDistrictDistrictCode(Integer districtCode);
}
