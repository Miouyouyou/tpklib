/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.exception;

import org.tizen.tpklib.TPK;
import org.tizen.tpklib.lib.Log;

public class TPKException
extends Exception {
    private static final long serialVersionUID = 1435508157651499267L;
    Log log = TPK.log;

    public TPKException() {
    }

    public TPKException(String msg) {
        super(msg);
        if (this.log != null) {
            this.log.error(msg);
        }
    }

    public TPKException(Throwable cause) {
        super(cause);
        if (this.log != null) {
            this.log.exception(cause);
        }
    }

    public TPKException(String msg, Throwable cause) {
        super(msg, cause);
        if (this.log != null) {
            this.log.error(msg);
            this.log.exception(cause);
        }
    }

    @Override
    public String getMessage() {
        String msg = super.getMessage();
        if (msg != null && !msg.isEmpty()) {
            return msg;
        }
        Throwable exception = this.getCause();
        if (exception != null) {
            return exception.getMessage();
        }
        return this.toString();
    }
}

