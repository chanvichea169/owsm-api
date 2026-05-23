package com.owsm.AuthService;

import com.owsm.AuthService.enumeration.RoleName;
import com.owsm.AuthService.model.Role;
import com.owsm.AuthService.repository.RoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class AuthServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthServiceApplication.class, args);
	}

	@Bean
	CommandLineRunner seedRoles(RoleRepository roleRepository) {
		return args -> {
			for (RoleName roleName : new RoleName[]{
					RoleName.ADMIN,
					RoleName.USER,
					RoleName.HR,
					RoleName.OFFICER,
					RoleName.HEAD_OF_DEPARTMENT
			}) {
				if (roleRepository.findByName(roleName).isEmpty()) {
					Role role = new Role();
					role.setName(roleName);
					roleRepository.save(role);
				}
			}
		};
	}

}
