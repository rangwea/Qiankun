package com.wikia.calabash.auth;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.nio.charset.StandardCharsets.UTF_8;

@Data
@Slf4j
public class Signer {
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    private String appId;
    private String timestamp;
    private String traceId;
    private String sign;

    public Signer(String appId, String timestamp, String traceId, String sign) {
        this.appId = appId;
        this.timestamp = timestamp;
        this.traceId = traceId;
        this.sign = sign;
    }

    public static Signer sign(String appId, String appSecret) throws IOException {
        String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
        String traceId = RandomStringUtils.randomAlphanumeric(32);

        byte[] bytes = encryptMD5(appSecret + appId + traceId + timestamp + appSecret);
        String sign = byte2hex(bytes);

        log.info("createSign appId:{}, traceId:{}, timestamp:{} ===> sign: {}", appId, traceId, timestamp, sign);

        return new Signer(appId, timestamp, traceId, sign);
    }

    private static byte[] encryptMD5(String data) throws IOException {
        byte[] bytes;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            bytes = md.digest(data.getBytes(UTF_8));
        } catch (GeneralSecurityException gse) {
            throw new IOException(gse);
        }
        return bytes;
    }

    private static String byte2hex(byte[] bytes) {
        StringBuilder sign = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() == 1) {
                sign.append("0");
            }
            sign.append(hex.toUpperCase());
        }
        return sign.toString();
    }

}