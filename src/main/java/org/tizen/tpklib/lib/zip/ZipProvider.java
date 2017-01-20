/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib.zip;

import org.tizen.tpklib.lib.Platform;
import org.tizen.tpklib.lib.zip.IZip;
import org.tizen.tpklib.lib.zip.ZipCommand;
import org.tizen.tpklib.lib.zip.ZipLibrary;

public class ZipProvider {
    public static IZip getZipInstance() {
        if (Platform.isLinux()) {
            return new ZipCommand();
        }
        return new ZipLibrary();
    }
}

