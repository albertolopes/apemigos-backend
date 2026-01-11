package org.apemigos.integrations.pix.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.apemigos.integrations.pix.dto.PixRequestDTO;
import org.apemigos.integrations.pix.dto.PixResponseDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Service
public class PixService {

    @Value("${pix.default.key}")
    private String defaultPixKey;

    @Value("${pix.default.merchant-name}")
    private String merchantNameDefault;

    @Value("${pix.default.merchant-city}")
    private String merchantCityDefault;

    public PixResponseDTO generateStaticPix(PixRequestDTO request) throws Exception {
        // Only `amount` is accepted in the request; use default constants for pixKey, merchantName and merchantCity
        Double amount = request != null && request.getAmount() != null ? request.getAmount() : 0.0d;
        String txid = UUID.randomUUID().toString().replaceAll("-", "").substring(0, 20);

        String payload = buildPayload(defaultPixKey, amount, merchantNameDefault, merchantCityDefault, txid);
        String crc = calculateCrc16(payload + "6304");
        String fullPayload = payload + "6304" + crc;

        // generate QR code
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix matrix = qrCodeWriter.encode(fullPayload, BarcodeFormat.QR_CODE, 400, 400);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(matrix, "PNG", baos);
        String base64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(baos.toByteArray());

        return PixResponseDTO.builder().payload(fullPayload).qrCodeBase64(base64).txid(txid).build();
    }

    private String buildPayload(String pixKey, Double amount, String merchantName, String merchantCity, String txid) {
        // Monta os campos de acordo com padrão EMV do PIX (versão simplificada)
        StringBuilder sb = new StringBuilder();
        append(sb, "00", "01"); // Payload Format Indicator
        append(sb, "26", buildGui(pixKey)); // Merchant Account Information (contains GUI + key)
        append(sb, "52", "0000"); // Merchant Category Code
        append(sb, "53", "986"); // Currency - BRL

        // somente adiciona o valor se for maior que zero (PIX estático com valor)
        if (amount != null && amount > 0.0d) {
            append(sb, "54", formatAmount(amount));
        }

        append(sb, "58", "BR");
        append(sb, "59", truncate(merchantName, 25));
        append(sb, "60", truncate(merchantCity, 15));

        // Campo 62 (Additional Data Field Template) com subtag 05 (txid)
        StringBuilder additional = new StringBuilder();
        append(additional, "05", txid);
        append(sb, "62", additional.toString());

        return sb.toString();
    }

    private String buildGui(String pixKey) {
        StringBuilder sb = new StringBuilder();
        append(sb, "00", "BR.GOV.BCB.PIX");
        // Tag 01 dentro de 26 deve conter o valor da chave (o append calcula o tamanho)
        append(sb, "01", pixKey);
        return sb.toString();
    }

    private String txidPayload(String txid) {
        // Tag 05 + length + txid value
        return String.format("05%02d%s", txid.length(), txid);
    }

    private void append(StringBuilder sb, String tag, String value) {
        sb.append(tag);
        sb.append(String.format("%02d", value.length()));
        sb.append(value);
    }

    private String formatAmount(Double amount) {
        if (amount == null || amount <= 0.0d) return "";
        // Use Locale.US to force dot as decimal separator
        return String.format(Locale.US, "%.2f", amount);
    }

    // CRC16-CCITT (X25) polynomial 0x1021 initial 0xFFFF, inverted final
    private String calculateCrc16(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        int crc = 0xFFFF;
        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) crc = (crc << 1) ^ 0x1021;
                else crc <<= 1;
                crc &= 0xFFFF;
            }
        }
        return String.format("%04X", crc);
    }

    private String truncate(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max);
    }
}
