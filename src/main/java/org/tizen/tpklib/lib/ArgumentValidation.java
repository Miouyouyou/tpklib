/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

public class ArgumentValidation {
    public static /* varargs */ boolean validateStringArgument(String ... args) {
        if (args == null) {
            return false;
        }
        for (String arg : args) {
            if (arg != null && !arg.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public static /* varargs */ String getArguments(String ... args) {
        if (args == null || args.length <= 0) {
            return null;
        }
        StringBuffer retArgs = new StringBuffer();
        retArgs.append("[");
        for (int i = 0; i < args.length; ++i) {
            retArgs.append(args[i]);
            if (i == args.length - 1) continue;
            retArgs.append(", ");
        }
        retArgs.append("]");
        return retArgs.toString();
    }
}

