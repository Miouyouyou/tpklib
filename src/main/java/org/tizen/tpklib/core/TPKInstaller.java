/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.core;

import java.io.File;
import java.io.IOException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.tizen.tpklib.core.TPKPolicy;
import org.tizen.tpklib.exception.TPKException;
import org.tizen.tpklib.lib.Command;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.Platform;
import org.tizen.tpklib.lib.file.FileUtil;
import org.tizen.tpklib.model.BuildOption;

public class TPKInstaller {
    public boolean install(String buildDir, Map<String, String> includeMap, List<String> excludeList) throws TPKException {
        if (buildDir == null || buildDir.isEmpty()) {
            throw new TPKException("Build directory is empty");
        }
        if (includeMap == null || includeMap.isEmpty()) {
            throw new TPKException("The list for package is empty");
        }
        this.removeExcludeList(buildDir, includeMap, excludeList);
        for (String src : includeMap.keySet()) {
            String dstPath = PathUtil.addPath(buildDir, ".tpk", includeMap.get(src));
            this.install(src, dstPath);
        }
        TPKInstaller.installExceptionsDirectory(PathUtil.addPath(buildDir, ".tpk"));
        return true;
    }

    private boolean install(String src, String dst) throws TPKException {
        if (src == null || dst == null) {
            throw new TPKException(String.format("Cannot install from \"%s\" to \"%s\"", src, dst));
        }
        File srcFile = new File(src);
        File dstFile = new File(dst);
        if (!srcFile.exists()) {
            if (!dstFile.exists()) {
                if (!dstFile.mkdirs()) {
                    throw new TPKException(String.format("Cannot create directory(%s)", dst));
                }
                return true;
            }
            if (dstFile.isDirectory()) {
                return true;
            }
            throw new TPKException(String.format("File do not exist(%s)", src));
        }
        if (srcFile.isDirectory()) {
            if (!dstFile.exists() || !dstFile.isDirectory()) {
                if (!dstFile.mkdirs()) {
                    throw new TPKException(String.format("Cannot create directory(%s)", dst));
                }
                return true;
            }
            if (dstFile.isDirectory()) {
                return true;
            }
        }
        if (dstFile.exists() && !dstFile.delete()) {
            throw new TPKException(String.format("Cannot delete file(%s)", dst));
        }
        File parentFile = dstFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            try {
                throw new TPKException(String.format("Cannot create directory(%s)", parentFile.getCanonicalPath()));
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
        }
        try {
            try {
                Path srcPath = srcFile.toPath();
                if (Files.isSymbolicLink(srcPath)) {
                    Files.createSymbolicLink(dstFile.toPath(), Files.readSymbolicLink(srcPath), new FileAttribute[0]);
                } else if (!Command.cp(srcFile.getCanonicalPath(), dstFile.getCanonicalPath())) {
                    throw new TPKException(String.format("Cannot copy from (%s) to (%s)", srcFile.getCanonicalPath(), dstFile.getCanonicalPath()));
                }
            }
            catch (InterruptedException e) {
                throw new TPKException(e);
            }
            return true;
        }
        catch (IOException e) {
            throw new TPKException(e);
        }
    }

    private void removeExcludeList(String buildDir, Map<String, String> includeMap, List<String> excludeList) throws TPKException {
        if (includeMap == null || excludeList == null) {
            return;
        }
        for (String excludeFilePath : excludeList) {
            try {
                includeMap.remove(FileUtil.getCanonicalPath(excludeFilePath));
                continue;
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
        }
    }

    public static void install(BuildOption bOpt) throws TPKException {
        TPKInstaller.install(bOpt.getIncludeList(), bOpt.getPackageDirPath());
    }

    public static void install(Map<String, String> installMap, String targetDir) throws TPKException {
        if (FileUtil.existsPath(targetDir)) {
            try {
                FileUtil.removeFile(targetDir);
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
        }
        for (String key : installMap.keySet()) {
            TPKInstaller.install(key, installMap.get(key), targetDir);
        }
        TPKInstaller.installExceptionsDirectory(targetDir);
    }

    public static void install(String src, String dstRelativePath, String targetDir) throws TPKException {
        if (src == null || dstRelativePath == null) {
            throw new TPKException(String.format("Cannot install from \"%s\" to \"%s\"", src, dstRelativePath));
        }
        File srcFile = new File(src);
        File dstFile = new File(targetDir, dstRelativePath);
        if (!srcFile.exists()) {
            if (!dstFile.exists()) {
                if (!dstFile.mkdirs()) {
                    throw new TPKException(String.format("Cannot create directory(%s)", dstRelativePath));
                }
                return;
            }
            if (dstFile.isDirectory()) {
                return;
            }
            throw new TPKException(String.format("File do not exist(%s)", src));
        }
        if (srcFile.isDirectory()) {
            if (!dstFile.exists() || !dstFile.isDirectory()) {
                if (!dstFile.mkdirs()) {
                    throw new TPKException(String.format("Cannot create directory(%s)", dstRelativePath));
                }
                return;
            }
            if (dstFile.isDirectory()) {
                return;
            }
        }
        if (Files.isSymbolicLink(srcFile.toPath())) {
            try {
                Files.copy(srcFile.toPath(), dstFile.toPath(), new CopyOption[0]);
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
        }
        if (dstFile.exists() && !dstFile.delete()) {
            throw new TPKException(String.format("Cannot delete file(%s)", dstRelativePath));
        }
        File parentFile = dstFile.getParentFile();
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            try {
                throw new TPKException(String.format("Cannot create directory(%s)", parentFile.getCanonicalPath()));
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
        }
        try {
            try {
                if (!Command.cp(srcFile.getCanonicalPath(), dstFile.getCanonicalPath())) {
                    throw new TPKException(String.format("Cannot copy from (%s) to (%s)", srcFile.getCanonicalPath(), dstFile.getCanonicalPath()));
                }
                TPKInstaller.setDefualtPermission(dstFile);
            }
            catch (InterruptedException e) {
                throw new TPKException(e);
            }
            return;
        }
        catch (IOException e) {
            throw new TPKException(e);
        }
    }

    public static void setDefualtPermission(File dstFile) throws IOException {
        if (Platform.isLinux()) {
            HashSet<PosixFilePermission> perms = new HashSet<PosixFilePermission>();
            perms.add(PosixFilePermission.OWNER_READ);
            perms.add(PosixFilePermission.OWNER_WRITE);
            perms.add(PosixFilePermission.OWNER_EXECUTE);
            perms.add(PosixFilePermission.GROUP_READ);
            perms.add(PosixFilePermission.GROUP_EXECUTE);
            perms.add(PosixFilePermission.OTHERS_READ);
            perms.add(PosixFilePermission.OTHERS_EXECUTE);
            Files.setPosixFilePermissions(dstFile.toPath(), perms);
        }
    }

    private static void installExceptionsDirectory(String packagingDir) throws TPKException {
        for (String exceptionDir : TPKPolicy.getEmptyDirectoryList()) {
            String path = PathUtil.addPath(packagingDir, exceptionDir);
            try {
                FileUtil.removeFileR(path);
                FileUtil.makeDirs(path);
                continue;
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
        }
    }
}

