package com.owsm.AuthService;

import com.owsm.AuthService.securityaudit.service.AuthSessionService;
import com.owsm.AuthService.securityaudit.repository.AuthSessionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OWSMApplicationTests {

	@Autowired
	private AuthSessionService authSessionService;

	@Autowired
	private AuthSessionRepository authSessionRepository;

	@Test
	void contextLoads() {
	}

	@Test
	@Transactional
	void createsAndPersistsSessionWithGeneratedIdentifier() {
		var identifiers = authSessionService.create(null);

		assertThat(identifiers.sessionId()).isNotNull();
		assertThat(identifiers.tokenId()).isNotNull();
		assertThat(authSessionRepository.findById(identifiers.sessionId())).isPresent();
	}

}
