package com.owsm.AuthService.repository;

import com.owsm.AuthService.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Existing Auth methods
    Optional<User> findByEmail(String email);
        boolean existsByTelegramChatIdAndIdNot(String telegramChatId, Long id);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByUsername(String username);
    List<User> findByDepartmentId(Long departmentId);
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.village v " +
            "LEFT JOIN FETCH v.commune c " +
            "LEFT JOIN FETCH c.district d " +
            "LEFT JOIN FETCH d.province p " +
            "WHERE u.id = :id")
    Optional<User> findByIdWithLocation(@Param("id") Long id);
    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.village v " +
            "LEFT JOIN FETCH v.commune c " +
            "LEFT JOIN FETCH c.district d " +
            "LEFT JOIN FETCH d.province p " +
            "WHERE u.email = :email")
    Optional<User> findByEmailWithLocation(@Param("email") String email);

    @Query("SELECT u FROM User u " +
            "LEFT JOIN FETCH u.village v " +
            "LEFT JOIN FETCH v.commune c " +
            "LEFT JOIN FETCH c.district d " +
            "LEFT JOIN FETCH d.province p " +
            "WHERE u.username = :username")
    Optional<User> findByUsernameWithLocation(@Param("username") String username);
    List<User> findByVillageVillageCode(String villageCode);
    long countByVillageVillageCode(String villageCode);
    List<User> findByVillageCommuneCommuneCode(Integer communeCode);
    List<User> findByVillageCommuneDistrictDistrictCode(Integer districtCode);
    List<User> findByVillageCommuneDistrictProvinceProvinceCode(Integer provinceCode);
}