package com.owsm.AuthService;

import com.owsm.AuthService.securityaudit.service.ClientRequestInfo;
import com.owsm.AuthService.securityaudit.service.ClientRequestInfoExtractor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ClientRequestInfoExtractorTests {

    @Test
    void ignoresForwardedHeadersWhenRemoteAddressIsNotTrusted() {
        ClientRequestInfoExtractor extractor = new ClientRequestInfoExtractor("10.0.0.0/8");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("203.0.113.9");
        request.addHeader("X-Forwarded-For", "198.51.100.10");

        ClientRequestInfo info = extractor.extract(request);

        assertEquals("203.0.113.9", info.ipAddress().getHostAddress());
        assertNull(info.forwardedIp());
    }

    @Test
    void resolvesClientAddressFromTrustedProxyChain() {
        ClientRequestInfoExtractor extractor = new ClientRequestInfoExtractor("10.0.0.0/8");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("10.0.0.2");
        request.addHeader("X-Forwarded-For", "198.51.100.10, 10.0.0.1");

        ClientRequestInfo info = extractor.extract(request);

        assertEquals("198.51.100.10", info.ipAddress().getHostAddress());
        assertEquals("198.51.100.10", info.forwardedIp().getHostAddress());
    }

    @Test
    void capturesBoundedDeviceAndUserAgentDetails() {
        ClientRequestInfoExtractor extractor = new ClientRequestInfoExtractor("");
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr("192.0.2.5");
        request.addHeader("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120.0.0.0 Safari/537.36");
        request.addHeader("X-Device-Id", "65f09b64-dd87-4e79-a1a2-58351dd4346f");
        request.addHeader("X-Device-Name", "Workstation");
        request.addHeader("X-Application", "admin-web");

        ClientRequestInfo info = extractor.extract(request);

        assertEquals("Chrome", info.browser());
        assertEquals("Windows", info.operatingSystem());
        assertEquals("DESKTOP", info.deviceType());
        assertEquals("Workstation", info.deviceName());
        assertEquals("admin-web", info.application());
        assertEquals("65f09b64-dd87-4e79-a1a2-58351dd4346f", info.deviceId().toString());
    }
}
