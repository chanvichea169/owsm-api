package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.Village;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VillageRepository extends JpaRepository<Village, String> {
    Village findByVillageCode(String villageCode);

    List<Village> findByCommuneCommuneCode(Integer communeCode);

    long countByCommuneCommuneCode(Integer communeCode);
}
