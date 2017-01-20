/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib.file;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class FileHashCode {
    public static final String SHA256_HASHCODE_ALGORITHM = "SHA-256";

    public static String getSHA256(String filePath) throws NoSuchAlgorithmException, IOException {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        return FileHashCode.getHashCode(filePath, "SHA-256");
    }

    public static String getSHA256(File file) throws NoSuchAlgorithmException, IOException {
        if (file == null || !file.exists() || file.isDirectory()) {
            return "";
        }
        return FileHashCode.getHashCode(file.getCanonicalPath(), "SHA-256");
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String getHashCode(String filePath, String algorithm) throws NoSuchAlgorithmException, IOException {
        MessageDigest mDigest = MessageDigest.getInstance(algorithm);
        File file = new File(filePath);
        if (!file.exists() || file.isDirectory()) {
            return "";
        }
        FileInputStream fInput = null;
        BufferedInputStream bInput = null;
        try {
            fInput = new FileInputStream(file);
            bInput = new BufferedInputStream(fInput);
            byte[] data = new byte[8192];
            int readBuf = 0;
            while ((readBuf = bInput.read(data)) > 0) {
                if (mDigest == null) continue;
                mDigest.update(data, 0, readBuf);
            }
            String string = FileHashCode.getHashCode(mDigest);
            return string;
        }
        finally {
            if (bInput != null) {
                bInput.close();
            }
            if (fInput != null) {
                fInput.close();
            }
        }
    }

    private static String getHashCode(MessageDigest digest) {
        if (digest == null) {
            return null;
        }
        byte[] data = digest.digest();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < data.length; ++i) {
            sb.append(Integer.toString((data[i] & 255) + 256, 16).substring(1));
        }
        return sb.toString();
    }
}

