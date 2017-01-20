/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib.file;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.StringUtil;
import org.tizen.tpklib.lib.file.FileUtil;

public class FileSearch {
    private static String WILDCARD = "*";
    private static String WILDCARD_REGEX = ".*?";

    public static List<String> getFileListFromExtension(String baseDir, String extension) throws IOException {
        if (baseDir == null) {
            throw new IllegalArgumentException();
        }
        return FileSearch.getFileListFromExtension(new File(baseDir), extension);
    }

    public static List<String> getFileListFromExtension(File baseFile, String extension) throws IOException {
        if (baseFile == null || extension == null) {
            throw new IllegalArgumentException();
        }
        ArrayList<String> retList = new ArrayList<String>();
        if (baseFile.isDirectory()) {
            for (File f : baseFile.listFiles()) {
                retList.addAll(FileSearch.getFileListFromExtension(f, extension));
            }
        } else if (baseFile.getCanonicalPath().endsWith(extension)) {
            retList.add(baseFile.getCanonicalPath());
        }
        return retList;
    }

    public static List<String> getFileList(String baseDir, String wildCardPath) throws IOException {
        if (baseDir == null) {
            return Collections.emptyList();
        }
        if (wildCardPath == null || wildCardPath.isEmpty()) {
            return FileSearch.getFileList(baseDir);
        }
        return FileSearch.getFileListFromWildcard(baseDir, wildCardPath);
    }

    public static List<String> getFileList(String path) throws IOException {
        if (path == null) {
            return Collections.emptyList();
        }
        if (path.contains("*")) {
            return FileSearch.getFileListFromWildcard(path);
        }
        return FileSearch.getFileList(new File(path));
    }

    private static List<String> getFileListFromWildcard(String path) throws IOException {
        String prefixPath = StringUtil.getPrefixFromSeperator(path, WILDCARD);
        String baseDir = null;
        baseDir = prefixPath.endsWith("\\") || prefixPath.endsWith("/") ? prefixPath : FileUtil.getParentDirectory(prefixPath);
        String wildcardPath = path.replace(WILDCARD, WILDCARD_REGEX).replace("\\", "/");
        List<String> baseDirFileList = FileSearch.getFileList(new File(baseDir));
        ArrayList<String> fileList = new ArrayList<String>();
        for (String filePath : baseDirFileList) {
            if (!(filePath = filePath.replace("\\", "/")).matches(wildcardPath)) continue;
            fileList.add(FileUtil.getOSPath(filePath));
        }
        return fileList;
    }

    private static List<String> getFileListFromWildcard(String baseDir, String wildcardPath) throws IOException {
        String fullPath = PathUtil.addPath(baseDir, wildcardPath);
        String regexPath = FileUtil.getCanonicalPath(fullPath).replace(WILDCARD, WILDCARD_REGEX);
        List<String> baseDirFileList = FileSearch.getFileList(new File(baseDir));
        ArrayList<String> fileList = new ArrayList<String>();
        for (String filePath : baseDirFileList) {
            if (!filePath.matches(regexPath)) continue;
            fileList.add(filePath.replace(baseDir, ""));
        }
        return fileList;
    }

    public static List<String> getFileListWithoutDirectory(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            return Collections.emptyList();
        }
        ArrayList<String> retList = new ArrayList<String>();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                if (f.isDirectory()) {
                    retList.addAll(FileSearch.getFileList(f));
                    continue;
                }
                retList.add(f.getCanonicalPath());
            }
        } else {
            retList.add(file.getCanonicalPath());
        }
        return retList;
    }

    public static List<String> getFileList(File file) throws IOException {
        if (file == null) {
            throw new NullPointerException();
        }
        if (!file.exists()) {
            return Collections.emptyList();
        }
        ArrayList<String> retList = new ArrayList<String>();
        if (file.isDirectory()) {
            for (File f : file.listFiles()) {
                retList.addAll(FileSearch.getFileList(f));
            }
        }
        retList.add(FileUtil.getOSPath(file.toString()));
        return retList;
    }

    public static List<File> getFileListFromLoop(File file) {
        ArrayList<File> retList = new ArrayList<File>();
        Stack<File> fileStack = new Stack<File>();
        fileStack.add(file);
        while (!fileStack.isEmpty()) {
            File popFile = (File)fileStack.pop();
            retList.add(popFile);
            if (!popFile.isDirectory()) continue;
            for (File f : popFile.listFiles()) {
                fileStack.add(f);
            }
        }
        return retList;
    }
}

