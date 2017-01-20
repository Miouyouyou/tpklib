/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.tizen.tpklib.exception.TPKException;
import org.tizen.tpklib.lib.ArgumentValidation;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.file.FileSearch;
import org.tizen.tpklib.lib.file.FileUtil;

public class TPKPolicy {
    private static boolean needEmptyDirectory = false;
    public static final List<String> TPK_STRIP_DIR_LIST = TPKPolicy.getStripDirList();
    public static final List<String> EXCEPTION_DIRECTORY_LIST = TPKPolicy.getEmptyDirectoryList();

    public static boolean getNeedEmptyDirectory() {
        return needEmptyDirectory;
    }

    public void setNeedEmptyDirectory(boolean needEmptyDirectory) {
        TPKPolicy.needEmptyDirectory = needEmptyDirectory;
    }

    TPKPolicy() {
    }

    TPKPolicy(boolean needEmptyDirectory) {
        TPKPolicy.needEmptyDirectory = needEmptyDirectory;
    }

    public static Map<String, String> getDefaultPackagingMap(String buildMode, String binDirName, String binFileName) {
        HashMap<String, String> dPackagingMap = new HashMap<String, String>();
        dPackagingMap.put("bin", "bin");
        dPackagingMap.put("lib", "lib");
        dPackagingMap.put("res", "res");
        dPackagingMap.put("data", "data");
        dPackagingMap.put("shared", "shared");
        dPackagingMap.put(PathUtil.addPath(buildMode, "res"), "res");
        dPackagingMap.put(PathUtil.addPath(buildMode, "shared"), "shared");
        dPackagingMap.put(PathUtil.addPath(buildMode, binFileName), PathUtil.addPath(binDirName, binFileName));
        dPackagingMap.put("tizen-manifest.xml", "tizen-manifest.xml");
        return dPackagingMap;
    }

    public static List<String> getProjectMandatoryList(String buildMode, String binFileName) {
        ArrayList<String> mList = new ArrayList<String>();
        mList.add(PathUtil.addPath(buildMode, binFileName));
        mList.add("tizen-manifest.xml");
        return mList;
    }

    public static List<String> getPackagingMandatoryDirList() {
        ArrayList<String> mList = new ArrayList<String>();
        mList.add("bin");
        mList.add("lib");
        mList.add("res");
        mList.add("shared");
        mList.add(PathUtil.addPath("shared", "res"));
        mList.add(PathUtil.addPath("shared", "trusted"));
        mList.add(PathUtil.addPath("shared", "data"));
        return mList;
    }

    public static List<String> getMandatoryEmptyDirList() {
        ArrayList<String> mList = new ArrayList<String>();
        mList.add(PathUtil.addPath("shared", "trusted"));
        mList.add(PathUtil.addPath("shared", "data"));
        return mList;
    }

    public static List<String> getValidateDirList() {
        ArrayList<String> mList = new ArrayList<String>();
        mList.add("bin");
        mList.add("lib");
        mList.add("res");
        mList.add("shared");
        mList.add(PathUtil.addPath("shared", "res"));
        mList.add(PathUtil.addPath("shared", "trusted"));
        mList.add(PathUtil.addPath("shared", "data"));
        mList.add("tizen-manifest.xml");
        return mList;
    }

    public static List<String> getDefaultExcludeList() {
        ArrayList<String> mList = new ArrayList<String>();
        mList.add(PathUtil.addPath("res", "edje*", "*.edc"));
        mList.add(PathUtil.addPath("res", "po", "*.po"));
        mList.add(PathUtil.addPath("shared", "res", "edje*", "*.edc"));
        mList.add(PathUtil.addPath("shared", "res", "po", "*.po"));
        mList.add(PathUtil.addPath("res", "tep"));
        return mList;
    }

    public static List<String> getStripDirList() {
        ArrayList<String> dirList = new ArrayList<String>();
        dirList.add(PathUtil.addPath("bin"));
        dirList.add(PathUtil.addPath("lib"));
        dirList.add(PathUtil.addPath(PathUtil.addPath("shared", "lib")));
        dirList.add(PathUtil.addPath(PathUtil.addPath("shared", "bin")));
        return dirList;
    }

    public static List<String> getTEPDirList() {
        ArrayList<String> tepDirList = new ArrayList<String>();
        tepDirList.add(PathUtil.addPath("res", "tep"));
        return tepDirList;
    }

    public static List<String> getEmptyDirectoryList() {
        ArrayList<String> dirList = new ArrayList<String>();
        if (TPKPolicy.getNeedEmptyDirectory()) {
            dirList.add(PathUtil.addPath("shared", "data"));
            dirList.add(PathUtil.addPath("shared", "trusted"));
        }
        return dirList;
    }

    public static Map<String, String> getMandatoryIncludeList(String buildDir, String binFileName) {
        HashMap<String, String> mandatoryList = new HashMap<String, String>();
        return mandatoryList;
    }

    public static Map<String, String> getDefaultIncludeList(String buildType, String binFileName) {
        HashMap<String, String> defaultList = new HashMap<String, String>();
        defaultList.put("bin", "bin");
        defaultList.put("lib", "lib");
        defaultList.put("res", "res");
        defaultList.put(PathUtil.addPath("shared", "res"), PathUtil.addPath("shared", "res"));
        defaultList.put("shared", "shared");
        defaultList.put("tizen-manifest.xml", "tizen-manifest.xml");
        defaultList.put(PathUtil.addPath(buildType, "res"), "res");
        defaultList.put(PathUtil.addPath(buildType, "shared"), "shared");
        defaultList.put(PathUtil.addPath(buildType, binFileName), PathUtil.addPath("bin", binFileName));
        return defaultList;
    }

    public static List<String> getMandatoryExcludeList(String buildType) {
        ArrayList<String> mandatoryList = new ArrayList<String>();
        mandatoryList.add(PathUtil.addPath(buildType, "*.mk"));
        mandatoryList.add(PathUtil.addPath(buildType, "*.ninja"));
        return mandatoryList;
    }

    public static List<String> getDefaultExcludeList(String buildType) {
        ArrayList<String> defaultList = new ArrayList<String>();
        defaultList.add(PathUtil.addPath("res", "edje", "*.edc"));
        defaultList.add(PathUtil.addPath("res", "po", "*.po"));
        defaultList.add(PathUtil.addPath("shared", "res", "edje", "*.edc"));
        defaultList.add(PathUtil.addPath("shared", "res", "po", "*.po"));
        defaultList.add(PathUtil.addPath("res", "contents", "*.edc"));
        defaultList.add(PathUtil.addPath("res", "contents", "edje_res", "*"));
        defaultList.add(PathUtil.addPath("res", "contents", "edje_res"));
        defaultList.add(PathUtil.addPath("res", "contents", "*", "edje_res", "*"));
        defaultList.add(PathUtil.addPath("res", "contents", "*", "edje_res"));
        return defaultList;
    }

    public Map<String, String> getInstallList(String projectPath, String buildType, String binFileName, Map<String, String> userIncludeList, List<String> userExcludeList, boolean isTEP) throws TPKException {
        HashMap<String, String> installList = new HashMap<String, String>();
        if (!ArgumentValidation.validateStringArgument(projectPath, buildType, binFileName)) {
            return installList;
        }
        installList.putAll(this.getIncludeFileList(projectPath, TPKPolicy.getDefaultIncludeList(buildType, binFileName)));
        installList.putAll(this.getIncludeFileList(projectPath, userIncludeList));
        for (String filePath2222 : this.getExcludeFileList(projectPath, userExcludeList)) {
            installList.remove(filePath2222);
        }
        for (String filePath2222 : this.getExcludeFileList(projectPath, TPKPolicy.getDefaultExcludeList(buildType))) {
            installList.remove(filePath2222);
        }
        installList.putAll(this.getIncludeFileList(projectPath, TPKPolicy.getMandatoryIncludeList(buildType, binFileName)));
        for (String filePath2222 : this.getExcludeFileList(projectPath, TPKPolicy.getMandatoryExcludeList(buildType))) {
            installList.remove(filePath2222);
        }
        for (String filePath2222 : this.getExcludeFileList(projectPath, TPKPolicy.getEmptyDirectoryList())) {
            installList.remove(filePath2222);
        }
        for (String filePath2222 : this.getExcludeFileList(projectPath, TPKPolicy.getTEPDirList())) {
            installList.remove(filePath2222);
        }
        return installList;
    }

    private Map<String, String> getIncludeFileList(String projectPath, Map<String, String> includeList) throws TPKException {
        HashMap<String, String> includeFileList = new HashMap<String, String>();
        if (includeList == null) {
            return includeFileList;
        }
        for (String key : includeList.keySet()) {
            List<String> fileList;
            String path = this.getPathWithProjectPath(projectPath, key);
            try {
                fileList = FileSearch.getFileList(path);
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
            for (String file : fileList) {
                String relativeTargetPath = null;
                relativeTargetPath = file.replace(FileUtil.getOSPath(path), FileUtil.getOSPath(includeList.get(key)));
                includeFileList.put(file, relativeTargetPath);
            }
        }
        return includeFileList;
    }

    private List<String> getExcludeFileList(String projectPath, List<String> excludeList) throws TPKException {
        ArrayList<String> excludeFileList = new ArrayList<String>();
        if (excludeList == null) {
            return excludeFileList;
        }
        for (String excludePath : excludeList) {
            String path = this.getPathWithProjectPath(projectPath, excludePath);
            try {
                List<String> fList = FileSearch.getFileList(path);
                for (String filePath : fList) {
                    excludeFileList.add(filePath);
                    if (!FileUtil.isSymLink(filePath)) continue;
                    excludeFileList.add(FileUtil.getCanonicalPath(filePath));
                }
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
            if (!FileUtil.isSymLink(path)) continue;
            try {
                excludeFileList.addAll(FileSearch.getFileList(FileUtil.getCanonicalPath(path)));
                continue;
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
        }
        return excludeFileList;
    }

    private String getPathWithProjectPath(String projectPath, String path) throws TPKException {
        if (path.startsWith(projectPath)) {
            try {
                return FileUtil.getCanonicalPath(path);
            }
            catch (IOException e) {
                throw new TPKException(e);
            }
        }
        if (FileUtil.isAbsolute(path)) {
            return path;
        }
        String pkgPath = PathUtil.addPath(projectPath, path);
        return FileUtil.getOSPath(pkgPath);
    }

    public void cleanUpBeforePackage(String pkgDir) throws IOException {
        if (pkgDir == null || pkgDir.isEmpty()) {
            return;
        }
        for (String filePath : TPKPolicy.getEmptyDirectoryList()) {
            String eptDirPath = PathUtil.addPath(pkgDir, filePath);
            if (FileUtil.existsPath(eptDirPath)) {
                FileUtil.removeFile(eptDirPath);
            }
            FileUtil.createFile(eptDirPath);
        }
    }
}

