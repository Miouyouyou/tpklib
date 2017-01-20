/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.exception;

import org.tizen.tpklib.exception.RootstrapException;

public class RootstrapParsingException
extends RootstrapException {
    private static final long serialVersionUID = 6234847793188356836L;

    public RootstrapParsingException() {
    }

    public RootstrapParsingException(String msg) {
        super(msg);
    }

    public RootstrapParsingException(Throwable cause) {
        super(cause);
    }

    public RootstrapParsingException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

