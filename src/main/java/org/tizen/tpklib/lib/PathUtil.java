/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.tizen.tpklib.lib.Platform;
import org.tizen.tpklib.lib.file.FileUtil;

public class PathUtil {
    public static final String PARENT_RELATIVE_PATH = "..";
    public static final String TEMP_DIR_NAME = "temp";
    public static final String ASTERISK = "*";
    public static final String EXTENSION_SEPERATOR = ".";
    private static final String LINUX_ROOT_PATH = "/";
    private static final String LINUX_DIRECTORY_SEPERATOR = "/";
    private static final String WINDOWS_DRIVE_SEPERATOR = ":";
    private static final String WINDOWS_DIRECTORY_SEPERATOR = "\\";
    private static final String ENV_VAR_LOCAL_APP_DATA = "localappdata";
    public static final String XML_EXTENSION = "xml";

    public static String getHomePath() {
        return System.getProperty("user.home");
    }

    public static String getLocalAppData() {
        if (Platform.isLinux()) {
            return null;
        }
        return System.getenv("localappdata");
    }

    public static /* varargs */ String addPath(String ... path_components) {
        if (path_components.length <= 0 || path_components[0] == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer(path_components[0]);
        for (int i = 1; i < path_components.length; ++i) {
            if (path_components[i] == null) continue;
            sb.append(File.separator + path_components[i]);
        }
        return sb.toString();
    }

    public static String getRelativePath(String rootPath, String filePath) {
        if (rootPath == null) {
            File file = new File(filePath);
            return file.getPath();
        }
        if (filePath.indexOf(rootPath) != 0) {
            return filePath;
        }
        String relativePath = filePath.replace(rootPath, "");
        if (relativePath.charAt(0) == File.separatorChar) {
            return relativePath.substring(1);
        }
        return relativePath;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static String readAllTextFromFile(String filePath, boolean carrageReturn) throws IOException {
        StringBuffer txt;
        txt = new StringBuffer();
        FileInputStream is = null;
        BufferedReader in = null;
        try {
            String strLicense;
            is = new FileInputStream(filePath);
            in = new BufferedReader(new InputStreamReader((InputStream)is, "UTF-8"));
            while ((strLicense = in.readLine()) != null) {
                txt.append(strLicense);
                if (!carrageReturn) continue;
                txt.append("\n");
            }
        }
        finally {
            try {
                if (in != null) {
                    in.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (is != null) {
                    is.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        return txt.toString();
    }

    public static String getRootDrive(String filePath) {
        if (Platform.isWindows() && filePath.indexOf(":") == 1) {
            return filePath.substring(0, 2);
        }
        return null;
    }

    public static String getFileName(String filePath) {
        File file = new File(filePath);
        return file.getName();
    }

    public static String getFileNameWithoutExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        String fileName = PathUtil.getFileName(filePath);
        int index = fileName.lastIndexOf(".");
        if (index <= 0) {
            return fileName;
        }
        return fileName.substring(0, index);
    }

    public static String getExtension(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }
        String fileName = PathUtil.getFileName(filePath);
        int lastIndex = fileName.lastIndexOf(".");
        if (lastIndex < 0) {
            return "";
        }
        return fileName.substring(lastIndex + 1);
    }

    public static String getExtension(File filePath) throws IOException {
        if (filePath == null || !filePath.exists()) {
            return null;
        }
        if (filePath.isDirectory()) {
            return "";
        }
        return PathUtil.getExtension(filePath.getCanonicalPath());
    }

    public static String getParentDirectory(String filePath) throws IOException {
        File file = new File(filePath);
        return file.getParentFile().getCanonicalPath();
    }

    public static List<String> getChildFilePathList(File file) throws IOException {
        ArrayList<String> fileList = new ArrayList<String>();
        if (file == null || file.isFile()) {
            return fileList;
        }
        for (File childFile : file.listFiles()) {
            if (childFile.isFile()) {
                fileList.add(childFile.getCanonicalPath());
                continue;
            }
            fileList.addAll(PathUtil.getChildFilePathList(childFile));
        }
        return fileList;
    }

    public static String getCurrentPath() {
        return System.getProperty("user.dir");
    }

    public static String getOSPath(String path) throws IOException {
        if (path == null || path.isEmpty()) {
            return path;
        }
        File f = new File(path);
        return f.getCanonicalPath();
    }

    public static String getCorrectPath(String filePath) {
        if (Platform.isWindows() && filePath != null && filePath.length() >= 2 && filePath.startsWith("/") && filePath.charAt(2) == '/') {
            String correctPath = null;
            correctPath = filePath.substring(1);
            correctPath = correctPath.replaceFirst("/", ":\\");
            correctPath = correctPath.replaceAll("/", "\\");
            return correctPath;
        }
        return filePath;
    }

    public static boolean existParentDirectory(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return PathUtil.existParentDirectory(new File(path));
    }

    public static boolean existParentDirectory(File filePath) {
        if (filePath == null) {
            return false;
        }
        if (filePath.getParentFile() != null && filePath.getParentFile().exists()) {
            return true;
        }
        return false;
    }

    public static boolean existExtensionInPath(String path, String extension) throws IOException {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return PathUtil.existExtensionInPath(new File(path), extension);
    }

    public static boolean existExtensionInPath(File filePath, String extension) throws IOException {
        if (filePath == null || !filePath.exists() || extension == null) {
            return false;
        }
        if (filePath.isFile()) {
            if (PathUtil.getExtension(filePath).contains(extension)) {
                return true;
            }
            return false;
        }
        for (File childFile : filePath.listFiles()) {
            if (!PathUtil.existExtensionInPath(childFile, extension)) continue;
            return true;
        }
        return false;
    }

    public static boolean existFileInPath(String path, String fileName) {
        if (path == null || path.isEmpty()) {
            return false;
        }
        return PathUtil.existFileInPath(new File(path), fileName);
    }

    public static boolean existFileInPath(File filePath, String fileName) {
        if (filePath == null || !filePath.exists()) {
            return false;
        }
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }
        File searchDir = null;
        searchDir = filePath.isFile() ? filePath.getParentFile() : filePath;
        if (searchDir == null) {
            return false;
        }
        for (File f : searchDir.listFiles()) {
            if (!f.getName().equals(fileName)) continue;
            return true;
        }
        return false;
    }

    public static String pathToRegularExpression(String path) {
        if (path == null) {
            return path;
        }
        return path.replace("\\", "\\\\").replace("*", ".*?");
    }

    public static boolean existsFile(String filePath) {
        return FileUtil.existsPath(filePath);
    }

    public static boolean isEmpty(String path) {
        if (path == null || path.isEmpty()) {
            return true;
        }
        return false;
    }
}

