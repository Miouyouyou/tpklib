/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.exception;

import org.tizen.tpklib.exception.TPKException;

public class ToolchainNotFoundException
extends TPKException {
    private static final long serialVersionUID = 3091672512929252190L;

    public ToolchainNotFoundException() {
    }

    public ToolchainNotFoundException(String msg) {
        super(msg);
    }

    public ToolchainNotFoundException(Throwable cause) {
        super(cause);
    }

    public ToolchainNotFoundException(String msg, Throwable cause) {
        super(msg, cause);
    }
}

