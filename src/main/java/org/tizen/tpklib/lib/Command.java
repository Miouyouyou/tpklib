/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.tizen.tpklib.constants.SDKConstants;
import org.tizen.tpklib.lib.CommandRunner;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.Platform;
import org.tizen.tpklib.lib.file.FileUtil;

public class Command {
    public static final int CMD_ERROR = -1;
    public static final String MKLINK_OPTION_DIRECTORY_JUNCTION = "/J";
    public static final String MKLINK_OPTION_FILE_HARD_LINK = "/H";

    public static boolean remove(String path) throws IOException, InterruptedException {
        if (!Command.fileExists(path)) {
            return true;
        }
        String rm = null;
        rm = Platform.isWindows() ? PathUtil.addPath(SDKConstants.SDK_TOOL_MSYS_BIN_DIR_PATH, "rm") : "rm";
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add(rm);
        cmd.add("-rf");
        cmd.add(path);
        CommandRunner cr = Command.execute(cmd, null, null, true);
        if (cr.getExitValue() == 0) {
            return true;
        }
        return false;
    }

    public static boolean rm(String path) throws IOException, InterruptedException {
        return Command.remove(path);
    }

    public static boolean mkdir(String path) throws IOException, InterruptedException {
        File dirFile = new File(path);
        if (dirFile.exists() && dirFile.isDirectory()) {
            return true;
        }
        return dirFile.mkdirs();
    }

    public static boolean copy(String src, String dst, String option) throws IOException, InterruptedException {
        String cp = null;
        cp = Platform.isWindows() ? PathUtil.addPath(SDKConstants.SDK_TOOL_MSYS_BIN_DIR_PATH, "cp") : "cp";
        if (new File(src).isDirectory() && !dst.endsWith(File.separator)) {
            dst = dst + File.separator;
        }
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add(cp);
        cmd.add(option);
        cmd.add(new File(src).getCanonicalPath());
        cmd.add(new File(dst).getCanonicalPath());
        CommandRunner cr = Command.execute(cmd, null, null, true);
        if (cr.getExitValue() == 0) {
            return true;
        }
        return false;
    }

    public static boolean copy(String src, String dst) throws IOException, InterruptedException {
        if (Platform.isMacOS()) {
            return Command.copy(src, dst, "-R");
        }
        return Command.copy(src, dst, "-rf");
    }

    public static boolean copy(String src, String dst, List<String> excludeList, List<String> includeList, boolean isHardLink) throws IOException, InterruptedException {
        if (!Command.fileExists(src)) {
            return false;
        }
        boolean bRet = true;
        bRet = isHardLink ? Command.hardLinkCopy(src, dst) : Command.copy(src, dst);
        if (!bRet) {
            return false;
        }
        if (excludeList != null) {
            for (String excludeFile : excludeList) {
                String excludeFilePath = PathUtil.addPath(dst, excludeFile);
                if (!new File(excludeFilePath).exists() || Command.rm(excludeFilePath)) continue;
                bRet = false;
            }
        }
        if (includeList != null) {
            for (String includeFile : includeList) {
                String includeSrcFilePath = PathUtil.addPath(src, includeFile);
                String includeDstFilePath = PathUtil.addPath(dst, includeFile);
                if (!new File(includeSrcFilePath).exists() || Command.hardLinkCopy(includeSrcFilePath, includeDstFilePath)) continue;
                bRet = false;
            }
        }
        return bRet;
    }

    public static boolean hardLinkCopy(String src, String dst) throws IOException, InterruptedException {
        if (Platform.isWindows()) {
            return Command.mklink(src, dst, null);
        }
        if (Platform.isLinux()) {
            return Command.copy(src, dst, "-rlf");
        }
        if (Platform.isMacOS()) {
            return Command.copy(src, dst, "-R");
        }
        return false;
    }

    public static boolean cp(String src, String dst) throws IOException, InterruptedException {
        return Command.copy(src, dst);
    }

    public static boolean move(String src, String dst) throws IOException, InterruptedException {
        if (!Command.fileExists(src)) {
            return false;
        }
        String mv = "mv";
        if (Platform.isWindows()) {
            mv = PathUtil.addPath(SDKConstants.SDK_TOOL_MSYS_BIN_DIR_PATH, mv);
        }
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add(mv);
        cmd.add("-f");
        cmd.add(src);
        cmd.add(dst);
        CommandRunner cr = Command.execute(cmd, null, null, true);
        if (cr.getExitValue() == 0) {
            return true;
        }
        return false;
    }

    public static boolean mv(String src, String dst) throws IOException, InterruptedException {
        return Command.move(src, dst);
    }

    public static boolean rsync(String src, String dst, List<String> includeList, List<String> excludeList, boolean isHardLink) throws IOException, InterruptedException {
        if (!Command.fileExists(src)) {
            return false;
        }
        String rsync = "rsync";
        if (Platform.isWindows()) {
            rsync = PathUtil.addPath(SDKConstants.SDK_TOOL_MSYS_BIN_DIR_PATH, rsync);
        }
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add(rsync);
        if (isHardLink) {
            cmd.add("-H");
        }
        if (includeList != null && includeList.size() > 0) {
            for (String includeFile : includeList) {
                cmd.add("include=" + includeFile);
            }
        }
        cmd.add(src);
        cmd.add(dst);
        CommandRunner cr = Command.execute(cmd, null, null, true);
        if (cr.getExitValue() == 0) {
            return true;
        }
        return false;
    }

    public static boolean mklink(String src, String dst, String option) throws IOException, InterruptedException {
        if (!Command.fileExists(src)) {
            return false;
        }
        if (option == null || option.isEmpty()) {
            File srcFile = new File(src);
            option = srcFile.isDirectory() ? "/J" : "/H";
        }
        String mklink = "mklink";
        ArrayList<String> cmd = new ArrayList<String>();
        cmd.add("cmd");
        cmd.add("/c");
        cmd.add(mklink);
        cmd.add(option);
        cmd.add(Command.getCorrectPath(dst));
        cmd.add(Command.getCorrectPath(src));
        CommandRunner cr = Command.execute(cmd, null, null, true);
        if (cr.getExitValue() == 0) {
            return true;
        }
        return false;
    }

    private static String getCorrectPath(String path) {
        File file = new File(path);
        return file.getAbsolutePath();
    }

    public static CommandRunner execute(List<String> cmd, String runningDir, Map<String, String> env, boolean waitable) throws IOException, InterruptedException {
        CommandRunner cRunner = new CommandRunner();
        if (!cRunner.execute(cmd, runningDir, env, true)) {
            return null;
        }
        if (waitable) {
            cRunner.waitFor();
        }
        return cRunner;
    }

    public static boolean fileExists(String filePath) {
        return FileUtil.existsPath(filePath);
    }
}

