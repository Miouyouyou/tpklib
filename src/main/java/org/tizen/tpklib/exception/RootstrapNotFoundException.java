/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.exception;

import org.tizen.tpklib.exception.RootstrapException;

public class RootstrapNotFoundException
extends RootstrapException {
    private static final long serialVersionUID = -6918122636723938700L;

    public RootstrapNotFoundException() {
    }

    public RootstrapNotFoundException(String msg) {
        super(msg);
    }

    public RootstrapNotFoundException(Throwable cause) {
        super(cause);
    }

    public RootstrapNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

