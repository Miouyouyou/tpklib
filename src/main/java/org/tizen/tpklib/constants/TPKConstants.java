/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.constants;

import org.tizen.tpklib.constants.SDKConstants;
import org.tizen.tpklib.lib.PathUtil;

public class TPKConstants {
    public static final int TPK_DEFAULT_FILE_PERMISSION = 493;
    public static final String TPK_DEFAULT_HASHCODE_ALGORITHM = "SHA-256";
    public static final String TPK_DIR_NAME = "tpk";
    public static final String TPK_BUILD_DIR_NAME = ".tpk";
    public static final String TPK_EXTENSION = ".tpk";
    public static final String TEP_EXTENSION = ".tep";
    public static final String TPK_REFERENCED_PACKAGING_DIR_NAME = ".packaging";
    public static final String EDJE_DIR_NAME = "edje";
    public static final String DEBUG_INFO_DIR_NAME = ".debuginfo";
    public static final String SIGN_DIR_NAME = ".sign";
    public static final String SIGN_AUTHOR_SIGNATURE_XML = "author-signature.xml";
    public static final String SIGN_SIGNATURE_XML = "signature1.xml";
    public static final String TPK_LOG_FILE_NAME = "tpk.log";
    public static final String PACKAGE_NAME_SEPERATOR = "-";
    public static final String PLATFORM_SEPERATOR = "-";
    public static final String RESOUCE_MANAGER_CONFIG_FILE_NAME = "res.xml";
    public static final String SBI_DIR_NAME = "smart-build-interface";
    public static final String SBI_BIN_PATH = TPKConstants.getSBIBinPath();
    public static final String SBI_HOME_PATH = TPKConstants.getSBIHomePath();
    public static final String SBI_PLUGIN_DIR_NAME = "plugins";
    public static final String SBI_PLUGIN_PATH = PathUtil.addPath(SBI_HOME_PATH, "plugins");
    public static final String SBI_TEMP_PATH = PathUtil.addPath(SBI_HOME_PATH, "tmp");

    private static String getSBIBinPath() {
        return PathUtil.addPath(SDKConstants.getInstalledPath(), "tools", "smart-build-interface", "bin");
    }

    private static String getSBIHomePath() {
        return PathUtil.addPath(SDKConstants.getInstalledPath(), "tools", "smart-build-interface");
    }
}

