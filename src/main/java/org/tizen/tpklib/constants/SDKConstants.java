/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.constants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.Properties;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.Platform;
import org.tizen.tpklib.lib.PropertyParser;
import org.tizen.tpklib.lib.file.FileUtil;

public class SDKConstants {
    public static final String BIN_DIR_NAME = "bin";
    public static final String TOOL_DIR_NAME = "tools";
    public static final String SHARED_DIR_NAME = "shared";
    public static final String PLATFORMS_DIR_NAME = "platforms";
    public static final String ONE_POINT_ZERO_DIR_NAME = "1.0";
    public static final String INFO_DIR_NAME = "info";
    public static final String TEMP_DIR_NAME = "tmp";
    public static final String USR_DIR_NAME = "usr";
    public static final String LIB_DIR_NAME = "lib";
    public static final String RESOURCE_DIR_NAME = "res";
    public static final String DIRECTORY_LOCALE_NAME = "locale";
    public static final String DEBUG_DIR_NAME = "debug";
    public static final String BUILD_DEBUG_DIR_NAME = "Debug";
    public static final String OPT_DIR_NAME = "opt";
    public static final String APPS_DIR_NAME = "apps";
    public static final String TEP_DIR_NAME = "tep";
    public static final String TIZEN_SDK_DATA_DIR_NAME = "tizen-sdk-data";
    public static final String MANIFEST_FILE_NAME = "manifest.xml";
    public static final String CORE_MANIFEST_FILE_NAME = "tizen-manifest.xml";
    public static final String MULTI_MANIFEST_FILE_NAME = "manifest_multi.xml";
    private static final String SDK_INFO_FILE_NAME = "sdk.info";
    public static final String SUB_DIR_MK_FILE_NAME = "subdir.mk";
    public static final String EXTENSION_MK = "mk";
    public static final String EXE_EXTENSION = ".exe";
    private static final String ZIP_FILE_NAME_FOR_LINUX = "zip";
    private static final String ZIP_FILE_NAME_FOR_WINDOWS = "zip.exe";
    public static final String ZIP_FILE_NAME = SDKConstants.getZipFileName();
    private static final String SDK_CONFIGURATION_FILE_NAME = "tizensdkpath";
    private static final String SDK_INSTALLED_PATH_KEY = "TIZEN_SDK_INSTALLED_PATH";
    private static final String SDK_DATA_PATH_KEY = "TIZEN_SDK_DATA_PATH";
    private static String USER_SDK_INSTALLED_PATH = null;
    private static String SDK_INSTALLED_PATH = null;
    private static final String INSTALL_MANAGER_CONFIG_DIR_NAME_PREFIX = ".installmanager";
    private static final String INSTALL_MANAGER_CONFIG_DIR_NAME = ".installmanager_2.4";
    private static final String INSTALL_MANAGER_CONFIG_HOME_PATH = SDKConstants.getInstallManagerConfigHomePath();
    private static Properties SDK_CONFIGURATION = SDKConstants.getSDKConfiguration();
    public static final String SDK_HOME_PATH = SDKConstants.getSDKHomePath();
    public static final String SDK_TOOL_MINGW_DIR_NAME = "mingw";
    public static final String SDK_TOOL_MSYS_DIR_NAME = "msys2";
    public static final String SDK_TOOL_DIR_PATH = PathUtil.addPath(SDKConstants.getInstalledPath(), "tools");
    public static final String SDK_PLATFORMS_PATH = PathUtil.addPath(SDKConstants.getInstalledPath(), "platforms");
    public static final String SDK_TOOL_MSYS_DIR_PATH = PathUtil.addPath(SDK_TOOL_DIR_PATH, "msys2", "usr");
    public static final String SDK_TOOL_MSYS_BIN_DIR_PATH = PathUtil.addPath(SDK_TOOL_MSYS_DIR_PATH, "bin");

    static Properties getSDKConfiguration() {
        String sdkInfoFilePath = SDKConstants.findSDKInfoFilePath();
        if (sdkInfoFilePath == null || !FileUtil.existsPath(sdkInfoFilePath)) {
            return null;
        }
        try {
            return PropertyParser.parsing(sdkInfoFilePath);
        }
        catch (FileNotFoundException e) {
            return new Properties();
        }
        catch (IOException e) {
            return new Properties();
        }
    }

    static String findSDKInfoFilePath() {
        String classPath = SDKConstants.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        String infoFilePath = SDKConstants.findSDKInfoFile(classPath);
        if (infoFilePath != null) {
            return infoFilePath;
        }
        infoFilePath = SDKConstants.findSDKInfoFile(PathUtil.getCurrentPath());
        if (infoFilePath != null) {
            return infoFilePath;
        }
        return SDKConstants.getFixedInfoFile();
    }

    static String findSDKInfoFile(String path) {
        if (path == null || path.isEmpty() || !FileUtil.existsPath(path)) {
            return null;
        }
        File file = new File(path);
        while (PathUtil.existParentDirectory(file)) {
            if (!PathUtil.existFileInPath(file = file.getParentFile(), "sdk.info")) continue;
            return PathUtil.addPath(file.getAbsolutePath(), "sdk.info");
        }
        return null;
    }

    static String getFixedInfoFile() {
        return PathUtil.addPath(INSTALL_MANAGER_CONFIG_HOME_PATH, "tizensdkpath");
    }

    public static String getInstalledPath() {
        if (USER_SDK_INSTALLED_PATH != null) {
            return USER_SDK_INSTALLED_PATH;
        }
        if (SDK_INSTALLED_PATH == null) {
            if (SDK_CONFIGURATION != null) {
                SDK_INSTALLED_PATH = SDK_CONFIGURATION.getProperty("TIZEN_SDK_INSTALLED_PATH");
            } else {
                return null;
            }
        }
        return SDK_INSTALLED_PATH;
    }

    static String getZipFileName() {
        if (Platform.isWindows()) {
            return "zip.exe";
        }
        return "zip";
    }

    static String getInstallManagerConfigHomePath() {
        String localAppDataPath = null;
        localAppDataPath = Platform.isWindows() ? PathUtil.getLocalAppData() : PathUtil.getHomePath();
        String configDir = PathUtil.addPath(localAppDataPath, ".installmanager_2.4");
        if (FileUtil.existsPath(configDir)) {
            return configDir;
        }
        String[] children = FileUtil.getChildren(localAppDataPath);
        if (children == null) {
            return null;
        }
        for (String child : children) {
            if (!FileUtil.isDirectory(child) || !child.startsWith(".installmanager")) continue;
            return child;
        }
        return null;
    }

    public static String getSDKDataPath() {
        String sdkDataPath = PathUtil.addPath(SDKConstants.getInstalledPath(), "sdk.info");
        if (sdkDataPath == null || !FileUtil.existsPath(sdkDataPath)) {
            return PathUtil.addPath(PathUtil.getHomePath(), "tizen-sdk-data");
        }
        Properties props = null;
        try {
            File sdkDataFile = new File(sdkDataPath);
            props = PropertyParser.parsing(sdkDataFile);
        }
        catch (FileNotFoundException e) {
            return null;
        }
        catch (IOException e) {
            return null;
        }
        if (props == null) {
            return null;
        }
        return props.getProperty("TIZEN_SDK_DATA_PATH");
    }

    static String getSDKHomePath() {
        if (Platform.isWindows()) {
            return PathUtil.getRootDrive(SDKConstants.getInstalledPath());
        }
        return PathUtil.getHomePath();
    }

    public static void setUserInstalledPath(String installedPath) {
        USER_SDK_INSTALLED_PATH = installedPath;
    }
}

