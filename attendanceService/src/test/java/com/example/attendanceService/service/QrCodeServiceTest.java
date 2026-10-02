package com.example.attendanceService.service;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QrCodeServiceTest {

    private final QrCodeService service = new QrCodeService();

    @Test
    void rendersPngForBadgeCode() {
        byte[] png = service.generatePng("EMP-DREFDR5EHX", 256);

        assertTrue(png.length > 100, "QR PNG should contain image data");
        assertArrayEquals(
            new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A},
            Arrays.copyOfRange(png, 0, 8),
            "Generated image should be a valid PNG"
        );
    }
}
