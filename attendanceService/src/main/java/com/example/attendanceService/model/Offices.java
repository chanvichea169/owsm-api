package com.example.attendanceService.model;

import com.example.attendanceService.common.audit.BaseEntity;

public class Offices extends BaseEntity {
    private Long id;
    private String name;
    private String code;
    private String address;
    private String phoneNumber;
    private String email;
    private Long companyId;
}
