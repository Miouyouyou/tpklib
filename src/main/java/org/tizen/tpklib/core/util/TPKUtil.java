/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.core.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.tizen.tpklib.TPK;
import org.tizen.tpklib.lib.ArgumentValidation;
import org.tizen.tpklib.lib.Log;
import org.tizen.tpklib.lib.file.FileUtil;

public class TPKUtil {
    private static Log log = TPK.log;

    public static String getTEPHashCodeFromPackageName(String pkgName) {
        String[] pkgInfos = TPKUtil.getPackageInfos(pkgName);
        if (pkgName.length() < 2) {
            return null;
        }
        return pkgInfos[1];
    }

    public static String[] getPackageInfos(String pkgName) {
        if (pkgName == null) {
            return null;
        }
        return pkgName.split("-");
    }

    public static List<String> getTEPPackageList(String buildDir) {
        if (!ArgumentValidation.validateStringArgument(buildDir)) {
            return Collections.emptyList();
        }
        if (!FileUtil.hasChildren(buildDir)) {
            return Collections.emptyList();
        }
        ArrayList<String> tepPackageList = new ArrayList<String>();
        File buildDirFile = new File(buildDir);
        for (File cFile : buildDirFile.listFiles()) {
            String fileName = cFile.getName();
            if (!cFile.isFile() || !fileName.endsWith(".tep")) continue;
            tepPackageList.add(fileName);
        }
        return tepPackageList;
    }
}

