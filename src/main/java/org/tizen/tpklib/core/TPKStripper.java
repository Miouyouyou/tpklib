/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.core;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.tizen.tpklib.TPK;
import org.tizen.tpklib.core.TPKPolicy;
import org.tizen.tpklib.exception.TPKException;
import org.tizen.tpklib.lib.Command;
import org.tizen.tpklib.lib.CommandRunner;
import org.tizen.tpklib.lib.Log;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.Printer;
import org.tizen.tpklib.lib.file.FileSearch;
import org.tizen.tpklib.lib.file.FileUtil;

public class TPKStripper {
    private Log log = TPK.log;
    private OutputStream outStream = null;
    private OutputStream errStream = null;

    public void strip(String pkgId, String projectPath, String buildType, String stripper) throws TPKException {
        if (TPKStripper.isStrip(stripper)) {
            this.stripping(projectPath, buildType, pkgId, stripper);
        }
    }

    public static boolean isStrip(String stripper) {
        if (stripper == null || stripper.isEmpty()) {
            return false;
        }
        if (!FileUtil.existsPath(stripper)) {
            return false;
        }
        return true;
    }

    private boolean stripping(String projectPath, String buildType, String pkgName, String stripper) throws TPKException {
        if (stripper == null || stripper.isEmpty()) {
            throw new TPKException("Cannot find strip tool => " + stripper);
        }
        String debugInfoDirPath = PathUtil.addPath(projectPath, buildType, ".debuginfo");
        if (new File(debugInfoDirPath).exists()) {
            try {
                if (!Command.rm(debugInfoDirPath)) {
                    throw new TPKException(String.format("Cannot remove directory(%s)", debugInfoDirPath));
                }
            }
            catch (Throwable e) {
                throw new TPKException(e);
            }
        }
        String debugInfoAppsDirPath = PathUtil.addPath(debugInfoDirPath, "usr", "lib", "debug", "opt", "usr", "apps");
        try {
            if (!Command.mkdir(debugInfoAppsDirPath)) {
                throw new TPKException(String.format("Cannot create directory(%s)", debugInfoAppsDirPath));
            }
        }
        catch (Throwable e) {
            throw new TPKException(e);
        }
        for (String stripDir : TPKPolicy.TPK_STRIP_DIR_LIST) {
            if (this.stripDirectory(PathUtil.addPath(projectPath, buildType), debugInfoAppsDirPath, pkgName, stripDir, stripper)) continue;
            throw new TPKException("Cannot strip directory => " + stripDir);
        }
        return true;
    }

    private boolean stripDirectory(String buildDir, String debugInfoAppsDirPath, String pkgName, String targetDir, String stripper) throws TPKException {
        String debugInfoDir = PathUtil.addPath(debugInfoAppsDirPath, pkgName, targetDir);
        try {
            if (!Command.mkdir(debugInfoDir)) {
                throw new TPKException(String.format("Cannot create directory(%s)", debugInfoDir));
            }
        }
        catch (Throwable e) {
            throw new TPKException(e);
        }
        String dstDir = PathUtil.addPath(buildDir, ".tpk", targetDir);
        File dstDirFile = new File(dstDir);
        if (!dstDirFile.exists()) {
            this.log.warn(String.format("\"%s\" do not exist", dstDir));
            return true;
        }
        List<String> fileList = null;
        try {
            fileList = FileSearch.getFileListWithoutDirectory(dstDirFile);
        }
        catch (IOException e) {
            this.log.exception(e);
            throw new TPKException(e);
        }
        if (fileList == null) {
            throw new TPKException(String.format("Strip file is empty in \"%s\"", dstDir));
        }
        for (String file : fileList) {
            if (file.equalsIgnoreCase(".") || file.equalsIgnoreCase("..")) continue;
            String debugInfoFile = null;
            try {
                debugInfoFile = PathUtil.addPath(debugInfoDir, PathUtil.getOSPath(file).substring(dstDirFile.getCanonicalPath().length())) + ".debug";
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
            this.createDebugInfo(stripper, file, debugInfoFile);
            this.stripFile(stripper, file);
        }
        return true;
    }

    public void createDebugInfo(String stripper, String srcFile, String dstFile) throws TPKException {
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add(stripper);
        cmd.add("--only-keep-debug");
        cmd.add("-o");
        cmd.add(dstFile);
        cmd.add(srcFile);
        this.log.log("Strip command => " + cmd);
        CommandRunner debugCR = null;
        try {
            debugCR = Command.execute(cmd, null, null, false);
            if (debugCR == null) {
                throw new TPKException(String.format("Cannot strip file(%s)", srcFile));
            }
            Printer.printCommandOutput(debugCR.getProcess(), this.outStream, this.errStream);
            debugCR.waitFor();
        }
        catch (Throwable e) {
            throw new TPKException(e);
        }
        this.log.log("exit value of strip command => " + debugCR.getExitValue());
    }

    public void stripFile(String stripper, String srcFile) throws TPKException {
        ArrayList<String> stripCommand = new ArrayList<String>();
        stripCommand.add(stripper);
        stripCommand.add("--strip-unneeded");
        stripCommand.add("--remove-section");
        stripCommand.add(".comment");
        stripCommand.add(srcFile);
        this.log.log("Strip command => " + stripCommand);
        CommandRunner stripCR = null;
        try {
            stripCR = Command.execute(stripCommand, null, null, false);
            if (stripCR == null) {
                throw new TPKException(String.format("Cannot strip file(%s)", srcFile));
            }
            Printer.printCommandOutput(stripCR.getProcess(), this.outStream, this.errStream);
            stripCR.waitFor();
        }
        catch (Throwable e) {
            throw new TPKException(e);
        }
        this.log.log("exit value of strip command => " + stripCR.getExitValue());
    }

    public void setOutputStream(OutputStream outStream) {
        this.outStream = outStream;
    }

    public void setErrorStream(OutputStream errStream) {
        this.errStream = errStream;
    }
}

