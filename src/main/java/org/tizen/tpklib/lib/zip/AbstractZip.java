/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib.zip;

import java.io.IOException;
import java.io.OutputStream;
import org.tizen.tpklib.lib.Printer;
import org.tizen.tpklib.lib.zip.IZip;

abstract class AbstractZip
implements IZip {
    protected OutputStream outStream = null;
    protected OutputStream errStream = null;

    AbstractZip() {
    }

    @Override
    public void setOutputStream(OutputStream outStream) {
        this.outStream = outStream;
    }

    public void setErrStream(OutputStream errStream) {
        this.errStream = errStream;
    }

    protected void printStream(String name, long size, String comment) throws IOException {
        if (this.outStream == null) {
            return;
        }
        String msg = String.format("  adding: %s \t\t (in=%,d) (%s) \n", name, size, comment);
        Printer.print(this.outStream, msg);
    }
}

