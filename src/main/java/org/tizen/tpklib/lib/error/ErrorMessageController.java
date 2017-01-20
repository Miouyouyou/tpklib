/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib.error;

public class ErrorMessageController {
    private static Throwable exception = null;
    private static String errMsg = null;
    private static final String UNKNOWN_ERROR = "Unknown error has occurred.";

    public static void setException(Throwable e) {
        exception = e;
    }

    public static void setErrorMsg(String errMsg) {
        ErrorMessageController.errMsg = errMsg;
    }

    public static String getErrorMsg() {
        if (exception != null) {
            return exception.getMessage();
        }
        if (errMsg != null && !errMsg.isEmpty()) {
            return errMsg;
        }
        return "Unknown error has occurred.";
    }
}

