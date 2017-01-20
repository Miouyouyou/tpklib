/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.io.FileNotFoundException;

public class StringUtil {
    public static /* varargs */ boolean validatePath(String ... paths) throws FileNotFoundException {
        if (paths == null) {
            throw new FileNotFoundException("Path is null");
        }
        for (String path : paths) {
            if (path != null && !path.isEmpty()) continue;
            throw new FileNotFoundException("");
        }
        return false;
    }

    public static String getPrefixFromSeperator(String target, String seperator) {
        if (target == null || seperator == null) {
            return "";
        }
        int index = target.indexOf(seperator);
        String prefix = target.substring(0, index);
        return prefix;
    }

    public static String getSuffixFromSeperator(String target, String seperator) {
        if (target == null || seperator == null) {
            return "";
        }
        int index = target.indexOf(seperator);
        String suffix = target.substring(index + 1, target.length());
        return suffix;
    }

    public static /* varargs */ String addWordWithSeperator(String seperator, String ... strComponents) {
        if (seperator == null) {
            return null;
        }
        if (strComponents == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        sb.append(strComponents[0]);
        for (int i = 1; i < strComponents.length; ++i) {
            if (strComponents[i] == null || strComponents[i].isEmpty()) continue;
            sb.append(seperator);
            sb.append(strComponents[i]);
        }
        return sb.toString();
    }
}

