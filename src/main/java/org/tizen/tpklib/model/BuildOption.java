/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.model;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.tizen.tpklib.core.sign.ISigner;

public class BuildOption {
    private static BuildOption bOption = new BuildOption();
    private boolean needEmptyDirectory;
    private String projectPath = null;
    private String pkgDirPath = null;
    private String buildType = null;
    private String binFileName = null;
    private String buildDir = null;
    private String arch = null;
    private String pkgName = null;
    private String pkgType = null;
    private String prjName = null;
    private String artifactName = null;
    private String pkgVersion = null;
    private String stripper = null;
    private boolean strIP = false;
    private boolean llvmIR = false;
    private ISigner signer = null;
    private boolean isSign = false;
    private boolean canTEPPackaging = false;
    private String tepPath = null;
    private List<String> excludeList = new CopyOnWriteArrayList<String>();
    private Map<String, String> includeMap = new ConcurrentHashMap<String, String>();

    public boolean getNeedEmptyDirectory() {
        return this.needEmptyDirectory;
    }

    public void setNeedEmptyDirectory(boolean needEmptyDirectory) {
        this.needEmptyDirectory = needEmptyDirectory;
    }

    public String getPackageDirPath() {
        return this.pkgDirPath;
    }

    public void setPackageDirPath(String pkgDirPath) {
        this.pkgDirPath = pkgDirPath;
    }

    public String getBuildType() {
        return this.buildType;
    }

    public void setbuildType(String buildType) {
        this.buildType = buildType;
    }

    public String getProjectPath() {
        return this.projectPath;
    }

    public void setProjectPath(String prjPath) {
        this.projectPath = prjPath;
    }

    public String getBinFileName() {
        return this.binFileName;
    }

    public void setBinFileName(String binFileName) {
        this.binFileName = binFileName;
    }

    public String getBuildDirectory() {
        return this.buildDir;
    }

    public void setBuildDirectory(String buildDir) {
        this.buildDir = buildDir;
    }

    public String getArch() {
        return this.arch;
    }

    public void setArch(String arch) {
        this.arch = arch;
    }

    public String getPkgName() {
        return this.pkgName;
    }

    public void setPkgName(String pkgName) {
        this.pkgName = pkgName;
    }

    public String getPkgType() {
        return this.pkgType;
    }

    public void setPkgType(String pkgType) {
        this.pkgType = pkgType;
    }

    public String getPrjName() {
        return this.prjName;
    }

    public void setPrjName(String prjName) {
        this.prjName = prjName;
    }

    public String getArtifactName() {
        return this.artifactName;
    }

    public void setArtifactName(String artifactName) {
        this.artifactName = artifactName;
    }

    public String getPkgVersion() {
        return this.pkgVersion;
    }

    public void setPkgVersion(String pkgVersion) {
        this.pkgVersion = pkgVersion;
    }

    public String getStripper() {
        return this.stripper;
    }

    public void setStripper(String stripper) {
        this.stripper = stripper;
    }

    public boolean isStrIP() {
        return this.strIP;
    }

    public void setStrIP(boolean strIP) {
        this.strIP = strIP;
    }

    public boolean isLlvmIR() {
        return this.llvmIR;
    }

    public void setLlvmIR(boolean llvmIR) {
        this.llvmIR = llvmIR;
    }

    public ISigner getSigner() {
        return this.signer;
    }

    public void setSigner(ISigner signer) {
        this.signer = signer;
    }

    public List<String> getExcludeList() {
        return this.excludeList;
    }

    public void addExcludeList(List<String> addList) {
        this.excludeList.addAll(addList);
    }

    public void removeExcludeList(List<String> removeList) {
        this.excludeList.removeAll(removeList);
    }

    public Map<String, String> getIncludeList() {
        return this.includeMap;
    }

    public void addIncludeMap(Map<String, String> addMap) {
        if (addMap == null) {
            return;
        }
        this.includeMap.putAll(addMap);
    }

    public void removeIncludeMap(String key) {
        this.includeMap.remove(key);
    }

    public void removeIncludeMap(Map<String, String> removeMap) {
        if (removeMap == null) {
            return;
        }
        for (String key : removeMap.keySet()) {
            this.includeMap.remove(key);
        }
    }

    public void setTEPPackaging(boolean tepPackaging) {
        this.canTEPPackaging = tepPackaging;
    }

    public boolean isTEPPackaging() {
        return this.canTEPPackaging;
    }

    public void setTEPDirPath(String path) {
        this.tepPath = path;
    }

    public String getTEPDirPath() {
        return this.tepPath;
    }

    public void setSign(boolean isSign) {
        this.isSign = isSign;
    }

    public boolean isSign() {
        return this.isSign;
    }

    public void clear() {
        this.strIP = false;
        this.llvmIR = false;
        this.canTEPPackaging = false;
        this.includeMap.clear();
        this.excludeList.clear();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("-Project path = " + this.projectPath + "\n");
        sb.append("-Package dir = " + this.pkgDirPath + "\n");
        sb.append("-Build type = " + this.buildType + "\n");
        sb.append("-Bin file name = " + this.binFileName + "\n");
        sb.append("-tepPath = " + this.tepPath + "\n");
        sb.append("-Build directory = " + this.buildDir + "\n");
        sb.append("-Architecture = " + this.arch + "\n");
        sb.append("-Package name = " + this.pkgName + "\n");
        sb.append("-Package version = " + this.pkgVersion + "\n");
        sb.append("-Package type = " + this.pkgType + "\n");
        sb.append("-Project name = " + this.prjName + "\n");
        sb.append("-Artifact name = " + this.artifactName + "\n");
        sb.append("-strIP = " + this.strIP + "\n");
        sb.append("-llvmIR = " + this.llvmIR + "\n");
        sb.append("-stripper = " + this.stripper + "\n");
        sb.append("-isSign = " + this.isSign + "\n");
        sb.append("-Signer Class = " + this.signer.toString() + "\n");
        
        return sb.toString();
    }

    
    public static BuildOption getInstance() {
        return bOption;
    }
}

