/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.exception;

import org.tizen.tpklib.exception.TPKException;

public class RootstrapException
extends TPKException {
    private static final long serialVersionUID = -6693898160361195579L;

    public RootstrapException() {
    }

    public RootstrapException(String msg) {
        super(msg);
    }

    public RootstrapException(Throwable cause) {
        super(cause);
    }

    public RootstrapException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

