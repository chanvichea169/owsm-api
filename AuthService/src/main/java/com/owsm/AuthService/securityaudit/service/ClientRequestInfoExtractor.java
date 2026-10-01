package com.owsm.AuthService.securityaudit.service;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ClientRequestInfoExtractor {
    private static final Pattern BROWSER_VERSION =
            Pattern.compile("(?:Edg|Edge|Chrome|CriOS|Firefox|FxiOS|Version|MSIE|rv):?/?([0-9]+(?:\\.[0-9]+)*)");
    private static final Pattern OS_VERSION =
            Pattern.compile("(?:Windows NT |Android |CPU (?:iPhone )?OS |Mac OS X )([0-9_\\.]+)");

    private final List<String> trustedProxyCidrs;

    public ClientRequestInfoExtractor(
            @Value("${security.audit.trusted-proxies:}") String trustedProxyConfig) {
        this.trustedProxyCidrs = trustedProxyConfig.isBlank()
                ? List.of()
                : List.of(trustedProxyConfig.split("\\s*,\\s*"));
    }

    public ClientRequestInfo currentRequest() {
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (!(attributes instanceof ServletRequestAttributes servletAttributes)) {
            return emptyRequestInfo();
        }
        return extract(servletAttributes.getRequest());
    }

    public ClientRequestInfo extract(HttpServletRequest request) {
        InetAddress remoteAddress = parseAddress(request.getRemoteAddr());
        boolean trustedProxy = isTrusted(remoteAddress);
        InetAddress clientAddress = trustedProxy
                ? forwardedClientAddress(request.getHeader("X-Forwarded-For"), remoteAddress)
                : remoteAddress;
        InetAddress forwardedAddress = trustedProxy
                ? forwardedClientAddress(request.getHeader("X-Forwarded-For"), remoteAddress)
                : null;
        if (trustedProxy && forwardedAddress == null) {
            forwardedAddress = parseAddress(request.getHeader("X-Real-IP"));
            if (forwardedAddress != null) {
                clientAddress = forwardedAddress;
            }
        }

        String userAgent = limit(request.getHeader("User-Agent"), 2048);
        String deviceName = limit(request.getHeader("X-Device-Name"), 128);
        String deviceType = detectDeviceType(userAgent);
        String browser = detectBrowser(userAgent);
        String browserVersion = detectVersion(userAgent, true);
        String operatingSystem = detectOperatingSystem(userAgent);
        String osVersion = detectVersion(userAgent, false);
        UUID deviceId = parseUuid(request.getHeader("X-Device-Id"));

        return new ClientRequestInfo(
                clientAddress,
                forwardedAddress,
                userAgent,
                deviceId,
                deviceName,
                deviceType,
                browser,
                browserVersion,
                operatingSystem,
                osVersion,
                limit(request.getHeader("X-Application"), 100),
                limit(request.getHeader("X-Platform"), 100),
                limit(request.getHeader("X-API-Version"), 64),
                Instant.now());
    }

    private ClientRequestInfo emptyRequestInfo() {
        return new ClientRequestInfo(
                null, null, null, null, null, null, null, null, null, null, null, null, null, Instant.now());
    }

    private InetAddress forwardedClientAddress(String forwardedFor, InetAddress remoteAddress) {
        if (forwardedFor == null || forwardedFor.isBlank()) {
            return null;
        }

        List<InetAddress> chain = new ArrayList<>();
        for (String value : forwardedFor.split(",")) {
            InetAddress address = parseAddress(value.trim());
            if (address == null) {
                return null;
            }
            chain.add(address);
        }
        if (remoteAddress != null) {
            chain.add(remoteAddress);
        }

        int index = chain.size() - 1;
        while (index > 0 && isTrusted(chain.get(index))) {
            index--;
        }
        return chain.get(index);
    }

    private boolean isTrusted(InetAddress address) {
        if (address == null) {
            return false;
        }
        return trustedProxyCidrs.stream().anyMatch(cidr -> matchesCidr(address, cidr));
    }

    private static boolean matchesCidr(InetAddress address, String cidr) {
        String[] parts = cidr.trim().split("/", 2);
        InetAddress network = parseAddress(parts[0]);
        if (network == null || network.getAddress().length != address.getAddress().length) {
            return false;
        }

        int prefix;
        try {
            prefix = parts.length == 1
                    ? network.getAddress().length * 8
                    : Integer.parseInt(parts[1]);
        } catch (NumberFormatException exception) {
            return false;
        }
        int maxPrefix = network.getAddress().length * 8;
        if (prefix < 0 || prefix > maxPrefix) {
            return false;
        }

        byte[] candidate = address.getAddress();
        byte[] networkBytes = network.getAddress();
        int wholeBytes = prefix / 8;
        int remainingBits = prefix % 8;
        for (int i = 0; i < wholeBytes; i++) {
            if (candidate[i] != networkBytes[i]) {
                return false;
            }
        }
        if (remainingBits == 0) {
            return true;
        }
        int mask = 0xff << (8 - remainingBits);
        return (candidate[wholeBytes] & mask) == (networkBytes[wholeBytes] & mask);
    }

    private static InetAddress parseAddress(String value) {
        if (value == null || !value.matches("[0-9a-fA-F:.]+")) {
            return null;
        }
        try {
            return InetAddress.getByName(value);
        } catch (UnknownHostException exception) {
            return null;
        }
    }

    private static UUID parseUuid(String value) {
        if (value == null || value.length() > 36) {
            return null;
        }
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private static String detectDeviceType(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        String lower = userAgent.toLowerCase();
        if (lower.contains("ipad") || lower.contains("tablet")) {
            return "TABLET";
        }
        if (lower.contains("mobile") || lower.contains("iphone") || lower.contains("android")) {
            return "MOBILE";
        }
        return "DESKTOP";
    }

    private static String detectBrowser(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        if (userAgent.contains("Edg/")) return "Edge";
        if (userAgent.contains("OPR/")) return "Opera";
        if (userAgent.contains("Chrome/") || userAgent.contains("CriOS/")) return "Chrome";
        if (userAgent.contains("Firefox/") || userAgent.contains("FxiOS/")) return "Firefox";
        if (userAgent.contains("Safari/")) return "Safari";
        return "Other";
    }

    private static String detectVersion(String userAgent, boolean browserVersion) {
        if (userAgent == null) {
            return null;
        }
        Matcher matcher = (browserVersion ? BROWSER_VERSION : OS_VERSION).matcher(userAgent);
        return matcher.find() ? matcher.group(1).replace('_', '.') : null;
    }

    private static String detectOperatingSystem(String userAgent) {
        if (userAgent == null) {
            return null;
        }
        String lower = userAgent.toLowerCase();
        if (lower.contains("windows")) return "Windows";
        if (lower.contains("android")) return "Android";
        if (lower.contains("iphone") || lower.contains("ipad") || lower.contains("ios")) return "iOS";
        if (lower.contains("mac os")) return "macOS";
        if (lower.contains("linux")) return "Linux";
        return "Other";
    }

    private static String limit(String value, int maxLength) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.length() <= maxLength ? trimmed : trimmed.substring(0, maxLength);
    }
}
