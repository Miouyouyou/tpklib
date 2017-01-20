/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib.file;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.tizen.tpklib.lib.ArgumentValidation;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.Platform;

public class FileUtil {
    public static final String ASTERIST = "*";

    public static File createFile(String path) throws IOException {
        return FileUtil.createTempFile(path, 0, "rw");
    }

    public static File createTempFiles(String path, long fileSize, int fileNum, String mode) throws IOException {
        if (fileNum <= 0) {
            throw new IOException(String.format("Invalid file number(%d)", fileNum));
        }
        FileUtil.createTempFile(path, fileSize, mode);
        for (int i = 1; i < fileNum; ++i) {
            FileUtil.createTempFile(path + i, fileSize, mode);
        }
        return null;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static File createTempFile(String path, long fileSize, String mode) throws IOException {
        FileUtil.validateCreateArgument(path, fileSize, mode);
        File file = new File(path);
        if (Files.exists(file.toPath(), new LinkOption[0])) {
            Files.delete(file.toPath());
        } else {
            File pFile = FileUtil.getParentDirectory(file);
            if (!Files.exists(pFile.toPath(), new LinkOption[0])) {
                Files.createDirectories(pFile.toPath(), new FileAttribute[0]);
            }
        }
        RandomAccessFile rFile = new RandomAccessFile(file, mode);
        try {
            rFile.setLength(fileSize);
        }
        finally {
            rFile.close();
        }
        return file;
    }

    private static void validateCreateArgument(String path, long fileSize, String mode) throws IOException {
        if (path == null || path.isEmpty()) {
            throw new IOException(String.format("Invalid file path(%s)", path));
        }
        if (fileSize < 0) {
            throw new IOException(String.format("Invalid file size(%d)", fileSize));
        }
        if (mode == null) {
            throw new IOException(String.format("Invalid file number(%s)", mode));
        }
    }

    public static boolean removeFileR(String path, String extension) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return FileUtil.removeFileR(new File(path), extension);
    }

    public static boolean removeFileR(File path, String extension) {
        boolean bRet = true;
        if (path == null || !path.exists()) {
            return false;
        }
        for (File cFile : path.listFiles()) {
            if (cFile.isDirectory()) {
                if (FileUtil.removeFileR(cFile, extension)) continue;
                bRet = false;
                continue;
            }
            String cExtension = PathUtil.getExtension(cFile.getName());
            if (!cExtension.equals(extension) || cFile.delete()) continue;
            bRet = false;
        }
        if (path.listFiles().length <= 0) {
            path.delete();
        }
        return bRet;
    }

    public static boolean removeFileR(File path, ArrayList<String> removeExtensionList, ArrayList<String> excludeDirList) {
        boolean bRet = true;
        if (path == null || !path.exists()) {
            return false;
        }
        for (File cFile : path.listFiles()) {
            if (cFile.isDirectory()) {
                if (FileUtil.existsPathInList(cFile.getAbsolutePath(), excludeDirList) || FileUtil.removeFileR(cFile, removeExtensionList, excludeDirList)) continue;
                bRet = false;
                continue;
            }
            String cExtension = PathUtil.getExtension(cFile.getName());
            if (!FileUtil.existsPathInList(cExtension, removeExtensionList) || cFile.delete()) continue;
            bRet = false;
        }
        return bRet;
    }

    public static boolean removeFileR(String filePath) throws IOException {
        return FileUtil.removeFile(filePath);
    }

    public static boolean removeFile(String filePath) throws IOException {
        if (filePath == null) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return true;
        }
        if (file.isFile()) {
            if (!file.delete()) {
                throw new IOException("Cannot remove file: " + file.getCanonicalPath());
            }
            return true;
        }
        return FileUtil.removeDir(file);
    }

    public static boolean removeFile(String baseDir, String relativePath) throws IOException {
        String fullPath = PathUtil.addPath(baseDir, relativePath);
        return FileUtil.removeFile(fullPath);
    }

    public static boolean removeFileList(List<String> fileList) throws IOException {
        for (String path : fileList) {
            if (FileUtil.removeFile(path)) continue;
            return false;
        }
        return true;
    }

    public static boolean removeFileList(String baseDir, List<String> relativePathList) throws IOException {
        for (String path : relativePathList) {
            if (FileUtil.removeFile(baseDir, path)) continue;
            return false;
        }
        return true;
    }

    public static boolean removeSubFileR(String rootPath) throws IOException {
        if (rootPath == null || rootPath.isEmpty()) {
            throw new IllegalArgumentException("Path = " + rootPath);
        }
        return FileUtil.removeSubFileR(new File(rootPath));
    }

    public static boolean removeSubFileR(File rootFile) throws IOException {
        if (rootFile == null) {
            throw new IllegalArgumentException("Path = " + rootFile);
        }
        if (!rootFile.exists()) {
            return true;
        }
        String[] childList = rootFile.list();
        if (childList == null || childList.length <= 0) {
            return true;
        }
        for (String childFile : rootFile.list()) {
            if (FileUtil.removeFileR(childFile)) continue;
            throw new IOException("Cannot remove direcotry: " + childFile);
        }
        return true;
    }

    private static boolean existsPathInList(String path, ArrayList<String> list) {
        for (String l : list) {
            File lFile = new File(l);
            File pathFile = new File(path);
            if (!lFile.getAbsolutePath().equals(pathFile.getAbsolutePath())) continue;
            return true;
        }
        return false;
    }

    public static String[] getChildren(String path) {
        if (path == null) {
            return new String[0];
        }
        return new File(path).list();
    }

    private static boolean removeDir(File dirFile) throws IOException {
        if (dirFile == null || !dirFile.exists()) {
            return true;
        }
        if (FileUtil.isEmptyDir(dirFile)) {
            if (!dirFile.delete()) {
                throw new IOException("Cannot remove directory: " + dirFile.getCanonicalPath());
            }
            return true;
        }
        for (File cFile : dirFile.listFiles()) {
            if (cFile.isFile()) {
                if (cFile.delete()) continue;
                throw new IOException("Cannot remove file: " + cFile.getCanonicalPath());
            }
            if (FileUtil.removeDir(cFile)) continue;
            throw new IOException("Cannot remove directory: " + cFile.getCanonicalPath());
        }
        if (!dirFile.delete()) {
            throw new IOException("Cannot remove directory: " + dirFile.getCanonicalPath());
        }
        return true;
    }

    private static boolean isEmptyDir(File dirFile) {
        File[] files = dirFile.listFiles();
        if (files == null || files.length <= 0) {
            return true;
        }
        return false;
    }

    public static void removeEmptyDir(String dirPath) {
        if (dirPath == null) {
            throw new IllegalArgumentException("Path is null");
        }
        FileUtil.removeEmptyDir(new File(dirPath));
    }

    public static void removeEmptyDir(File dirPath) {
        if (dirPath == null) {
            throw new IllegalArgumentException("Path is null");
        }
        if (!dirPath.isDirectory()) {
            return;
        }
        if (!dirPath.exists()) {
            return;
        }
        for (File childFile : dirPath.listFiles()) {
            if (!childFile.isDirectory()) continue;
            FileUtil.removeEmptyDir(childFile);
        }
        if (dirPath.listFiles().length <= 0) {
            dirPath.delete();
        }
    }

    public static boolean makeDirs(String path) throws IOException {
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("Path = " + path);
        }
        return FileUtil.makeDirs(new File(path));
    }

    public static boolean makeDirs(File dirPath) throws IOException {
        if (dirPath == null) {
            throw new IllegalArgumentException("Cannot make directory: " + dirPath);
        }
        if (dirPath.exists()) {
            throw new IOException(String.format("Cannot make directory. \"%s\" already exist", dirPath));
        }
        return dirPath.mkdirs();
    }

    public static String getCanonicalPath(String path) throws IOException {
        if (path == null) {
            return null;
        }
        if (path.contains("*")) {
            return path;
        }
        File f = new File(path);
        return f.getCanonicalPath();
    }

    public static String getParentDirectory(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            throw new IOException(String.format("Invalid file path(%s)", filePath));
        }
        File file = FileUtil.getParentDirectory(new File(filePath));
        return file.getCanonicalPath();
    }

    public static File getParentDirectory(File file) throws IOException {
        if (file == null) {
            throw new IOException(String.format("Invalid file path(%s)", file));
        }
        return file.getParentFile();
    }

    public static boolean renameTo(String src, String dst) {
        if (src == null || src.isEmpty()) {
            return false;
        }
        if (dst == null || dst.isEmpty()) {
            return false;
        }
        File srcFile = new File(src);
        return srcFile.renameTo(new File(dst));
    }

    public static String getLastModified(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return FileUtil.getLastModified(new File(filePath));
    }

    public static String getLastModified(File file) {
        if (file == null) {
            return null;
        }
        return Long.toString(file.lastModified());
    }

    public static boolean existsPath(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists();
    }

    public static boolean hasChildren(String filePath) {
        if (!ArgumentValidation.validateStringArgument(filePath)) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        String[] children = file.list();
        if (children == null) {
            return false;
        }
        if (children.length <= 0) {
            return false;
        }
        return true;
    }

    public static boolean isAbsolute(String filePath) {
        File f = FileUtil.getFile(filePath);
        return f.isAbsolute();
    }

    public static boolean isDirectory(String filePath) {
        File f = FileUtil.getFile(filePath);
        if (f.exists() && f.isDirectory()) {
            return true;
        }
        return false;
    }

    public static boolean isSymLink(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        if (path.contains("*")) {
            return false;
        }
        return FileUtil.isSymLink(new File(path));
    }

    public static boolean isSymLink(File file) {
        if (file == null) {
            return false;
        }
        return FileUtil.isSymLink(file.toPath());
    }

    public static boolean isSymLink(Path path) {
        if (path == null) {
            return false;
        }
        return Files.isSymbolicLink(path);
    }

    private static File getFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            throw new IllegalArgumentException(filePath);
        }
        return new File(filePath);
    }

    public static String getOSPath(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        if (Platform.isWindows()) {
            return path.replace("/", "\\");
        }
        File fPath = new File(path);
        Stack<String> paths = new Stack<String>();
        do {
            paths.push(fPath.getName());
        } while ((fPath = fPath.getParentFile()) != null);
        StringBuffer osPath = new StringBuffer((String)paths.pop());
        while (!paths.isEmpty()) {
            osPath.append(File.separator);
            osPath.append((String)paths.pop());
        }
        return osPath.toString();
    }
}

