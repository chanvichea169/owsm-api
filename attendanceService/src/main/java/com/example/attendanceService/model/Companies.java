package com.example.attendanceService.model;

import com.example.attendanceService.common.audit.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(name = "companies")
public class Companies extends BaseEntity {

    @Id
    private Long id;
    private String name;
    private String code;
    private String address;
    private String phoneNumber;
    private String email;
}
