/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

public interface IZip {
    public boolean zipping(File var1, String var2, int var3) throws FileNotFoundException, IOException, InterruptedException;

    public void setOutputStream(OutputStream var1);
}

