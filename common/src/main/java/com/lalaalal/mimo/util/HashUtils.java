package com.lalaalal.mimo.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public class HashUtils {
    private static final MessageDigest SHA1 = getMessageDigest("SHA-1");

    private static MessageDigest getMessageDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException exception) {
            throw new RuntimeException("Failed to initialize", exception);
        }
    }

    public static String hashFile(Path path) throws IOException {
        try (DigestInputStream digestInputStream = new DigestInputStream(Files.newInputStream(path), SHA1)) {
            byte[] buffer = new byte[16384];
            int read = 0;
            while (read != -1)
                read = digestInputStream.read(buffer, 0, 16384);
            return HexFormat.of().formatHex(SHA1.digest());
        }
    }
}
