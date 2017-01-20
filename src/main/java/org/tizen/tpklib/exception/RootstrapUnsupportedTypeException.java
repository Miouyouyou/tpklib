/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.exception;

import org.tizen.tpklib.exception.RootstrapException;

public class RootstrapUnsupportedTypeException
extends RootstrapException {
    private static final long serialVersionUID = 4235378645926214935L;

    public RootstrapUnsupportedTypeException() {
    }

    public RootstrapUnsupportedTypeException(String msg) {
        super(msg);
    }

    public RootstrapUnsupportedTypeException(Throwable cause) {
        super(cause);
    }

    public RootstrapUnsupportedTypeException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

