package com.example.attendanceService.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import org.springframework.stereotype.Service;

/** Renders QR codes as PNG images for employee attendance badges. */
@Service
public class QrCodeService {

    public byte[] generatePng(String content, int size) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("QR content must not be blank");
        }
        int dimension = Math.max(64, Math.min(size, 1024));
        try {
            BitMatrix matrix = new QRCodeWriter().encode(
                content,
                BarcodeFormat.QR_CODE,
                dimension,
                dimension,
                Map.of(
                    EncodeHintType.MARGIN, 1,
                    EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M
                )
            );
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(matrix, "PNG", output);
            return output.toByteArray();
        } catch (WriterException | IOException exception) {
            throw new IllegalStateException("Unable to render the QR code", exception);
        }
    }
}
