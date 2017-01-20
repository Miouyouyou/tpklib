/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib.zip;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.tizen.tpklib.lib.Command;
import org.tizen.tpklib.lib.CommandRunner;
import org.tizen.tpklib.lib.Printer;
import org.tizen.tpklib.lib.StringUtil;
import org.tizen.tpklib.lib.zip.AbstractZip;

public class ZipCommand
extends AbstractZip {
    @Override
    public boolean zipping(File file, String outFilePath, int permission) throws FileNotFoundException, IOException, InterruptedException {
        if (file == null || !file.exists()) {
            throw new FileNotFoundException();
        }
        List<String> command = this.getZipCommand(file.toString(), outFilePath);
        CommandRunner cr = null;
        cr = Command.execute(command, file.getCanonicalPath(), null, false);
        if (cr == null) {
            return false;
        }
        this.printCommandOutput(cr);
        cr.waitFor();
        if (cr.getExitValue() == 0) {
            return true;
        }
        return false;
    }

    private List<String> getZipCommand(String srcDir, String destFilePath) throws FileNotFoundException {
        ArrayList<String> cmd = new ArrayList<String>();
        if (StringUtil.validatePath(srcDir, destFilePath)) {
            return cmd;
        }
        cmd.add("zip");
        if (this.containsSymlinks(new File(srcDir))) {
            cmd.add("--symlinks");
        }
        String defaultOpt = "-rv";
        cmd.add(defaultOpt);
        cmd.add(destFilePath);
        cmd.add(".");
        return cmd;
    }

    private boolean containsSymlinks(File srcFile) {
        Path srcPath = srcFile.toPath();
        if (Files.isSymbolicLink(srcPath)) {
            return true;
        }
        if (srcFile.isFile()) {
            return false;
        }
        for (File cFile : srcFile.listFiles()) {
            if (!this.containsSymlinks(cFile)) continue;
            return true;
        }
        return false;
    }

    private void printCommandOutput(CommandRunner cr) throws InterruptedException {
        Thread outputThread = null;
        if (this.outStream != null) {
            outputThread = Printer.printFromInput(cr.getInputStream(), this.outStream);
        }
        Thread errThread = null;
        if (this.errStream != null) {
            errThread = Printer.printFromInput(cr.getErrorStream(), this.errStream);
        }
        if (outputThread != null) {
            outputThread.join();
        }
        if (errThread != null) {
            errThread.join();
        }
    }
}

