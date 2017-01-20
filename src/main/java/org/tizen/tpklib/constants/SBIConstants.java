/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.constants;

import org.tizen.tpklib.constants.SDKConstants;
import org.tizen.tpklib.lib.PathUtil;

public class SBIConstants {
    public static final String SBI_DIR_NAME = "smart-build-interface";
    public static final String SBI_PLUGIN_DIR_NAME = "plugins";
    public static final String SBI_LOG_FILE_NAME = "sbi.log";

    public static String getSBIHomePath() {
        return PathUtil.addPath(SDKConstants.getInstalledPath(), "tools", "smart-build-interface");
    }

    public static String getSBIPluginPath() {
        return PathUtil.addPath(SBIConstants.getSBIHomePath(), "plugins");
    }
}

