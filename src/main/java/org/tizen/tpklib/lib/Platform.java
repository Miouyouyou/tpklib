/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.io.IOException;
import org.tizen.tpklib.lib.PathUtil;

public class Platform {
    public static final int UNKNOWN = 0;
    public static final int LINUX_32 = 1;
    public static final int LINUX_64 = 4;
    public static final int WINDOWS_32 = 8;
    public static final int WINDOWS_64 = 16;
    public static final int MACOS_32 = 32;
    public static final int MACOS_64 = 64;
    public static final int UBUNTU_32 = 128;
    public static final int UBUNTU_64 = 256;
    public static final int UNSUPPORTED_PLATFORM = -1;
    public static final int GENERIC_LINUX = 389;
    public static final int GENERIC_WINDOWS = 24;
    public static final int GENERIC_MACOS = 96;
    public static final int GENERIC_UBUNTU = 384;
    public static final String DATA_MODEL_32 = "32";
    public static final String DATA_MODEL_64 = "64";
    public static final int CURRENT_PLATFORM = Platform.getCurrentPlatform();
    public static final String CURRENT_CHARSET = Platform.getCurrentCharset();

    private static int getCurrentPlatform() {
        String os = System.getProperty("os.name");
        String dataModel = System.getProperty("sun.arch.data.model");
        if (os.startsWith("Linux")) {
            if (dataModel.equals("32")) {
                return 1;
            }
            if (dataModel.equals("64")) {
                return 4;
            }
            return -1;
        }
        if (os.startsWith("Windows")) {
            if (dataModel.equals("32")) {
                return 8;
            }
            if (dataModel.equals("64")) {
                return 16;
            }
            return -1;
        }
        if (os.startsWith("Mac OS")) {
            return 64;
        }
        return -1;
    }

    private static String getCurrentCharset() {
        return System.getProperty("sun.jnu.encoding");
    }

    public static String getPlatformInfo() {
        String info = "";
        info = info + "OS Name: " + System.getProperty("os.name") + "\n";
        info = info + "OS Version: " + System.getProperty("os.version") + "\n";
        info = info + "Data Model: " + System.getProperty("sun.arch.data.model") + "\n";
        if (CURRENT_PLATFORM == 1) {
            String filePath = "/etc/lsb-release";
            try {
                info = info + PathUtil.readAllTextFromFile(filePath, true) + "\n";
            }
            catch (IOException var2_2) {
                // empty catch block
            }
        }
        info = info + "java : " + System.getProperty("java.runtime.name") + "\n";
        info = info + "vm version : " + System.getProperty("java.vm.version") + "\n";
        return info;
    }

    public static boolean isLinux() {
        return (CURRENT_PLATFORM & 389) > 0;
    }

    public static boolean isUbuntu() {
        return Platform.isLinux();
    }

    public static boolean isMacOS() {
        return (CURRENT_PLATFORM & 96) > 0;
    }

    public static boolean isWindows() {
        return (CURRENT_PLATFORM & 24) > 0;
    }
}

