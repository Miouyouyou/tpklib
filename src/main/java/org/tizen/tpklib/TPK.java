/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.tizen.tpklib.constants.SDKConstants;
import org.tizen.tpklib.core.TPKSystem;
import org.tizen.tpklib.core.sign.ISigner;
import org.tizen.tpklib.exception.TPKErrorMessage;
import org.tizen.tpklib.exception.TPKException;
import org.tizen.tpklib.lib.ArgumentValidation;
import org.tizen.tpklib.lib.CommandRunner;
import org.tizen.tpklib.lib.Log;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.Performance;
import org.tizen.tpklib.lib.error.ErrorMessageController;
import org.tizen.tpklib.lib.file.FileUtil;
import org.tizen.tpklib.model.BuildOption;

public class TPK {
    public static Log log = TPK.createLog();
    private OutputStream outStream = null;
    private OutputStream errorStream = null;
    private boolean needEmptyDirectory;

    public static Log createLog() {
        String logPath = PathUtil.addPath(SDKConstants.getSDKDataPath(), ".tpk", "tpk.log");
        return Log.createLog(logPath);
    }

    public void packaging(BuildOption bOption) throws TPKException {
        this.needEmptyDirectory = bOption.getNeedEmptyDirectory();
        this.packaging(bOption.getBuildDirectory(), bOption.getPkgName(), bOption.getPkgVersion(), bOption.getArch(), bOption.getArtifactName(), bOption.getIncludeList(), bOption.getExcludeList(), bOption.getStripper(), bOption.isSign(), bOption.getSigner(), null, null, this.needEmptyDirectory);
    }

    public void packaging(String buildDir, String pkgId, String appVersion, String arch, String binFileName, Map<String, String> includeList, List<String> excludeList, String stripper, boolean isSign, ISigner signer, String profilePath, String profileName) throws TPKException {
        this.packaging(buildDir, pkgId, appVersion, arch, binFileName, includeList, excludeList, stripper, isSign, signer, profilePath, profileName, this.outStream, this.errorStream);
    }

    public void packaging(String buildDir, String pkgId, String appVersion, String arch, String binFileName, Map<String, String> includeList, List<String> excludeList, String stripper, boolean isSign, ISigner signer, String profilePath, String profileName, boolean needEmptyDirectory) throws TPKException {
        this.packaging(buildDir, pkgId, appVersion, arch, binFileName, includeList, excludeList, stripper, isSign, signer, profilePath, profileName, this.outStream, this.errorStream, needEmptyDirectory);
    }

    public void packaging(String buildDir, String pkgId, String appVersion, String arch, String binFileName, Map<String, String> userIncludeList, List<String> userExcludeList, String stripper, boolean isSign, ISigner signer, String profilePath, String profileName, OutputStream outStream, OutputStream errStream, boolean needEmptyDirectory) throws TPKException {
        TPKSystem tSystem = new TPKSystem();
        if (outStream != null) {
            tSystem.setOutputStream(outStream);
        }
        if (errStream != null) {
            tSystem.setErrorStream(errStream);
        }
        this.validateMandatoryArgument(buildDir, pkgId, appVersion, binFileName);
        tSystem.initialize(buildDir, binFileName, userIncludeList, userExcludeList, needEmptyDirectory);
        tSystem.install();
        tSystem.strip(pkgId, stripper);
        tSystem.signing(isSign, profilePath, profileName, signer);
        tSystem.packaging(buildDir, pkgId, appVersion, arch);
        tSystem.cleanTemporaryFiles(buildDir);
    }

    public void packaging(String buildDir, String pkgId, String appVersion, String arch, String binFileName, Map<String, String> userIncludeList, List<String> userExcludeList, String stripper, boolean isSign, ISigner signer, String profilePath, String profileName, OutputStream outStream, OutputStream errStream) throws TPKException {
        this.packaging(buildDir, pkgId, appVersion, arch, binFileName, userIncludeList, userExcludeList, stripper, isSign, signer, profilePath, profileName, outStream, errStream, false);
    }

    public void cleanAll(String buildDir, String pkgId, String appVersion, String arch) {
        TPKSystem tSystem = new TPKSystem();
        try {
            tSystem.cleanAll(buildDir, pkgId, appVersion, arch);
        }
        catch (TPKException e) {
            log.exception(e);
        }
    }

    private void validateMandatoryArgument(String buildDir, String pkgId, String appVersion, String binFileName) throws TPKException {
        if (!ArgumentValidation.validateStringArgument(buildDir, pkgId, appVersion, binFileName)) {
            throw new TPKException(String.format(TPKErrorMessage.INVALID_ARGUMENT_ERROR, ArgumentValidation.getArguments(buildDir, pkgId, appVersion, binFileName)));
        }
        if (!FileUtil.existsPath(buildDir)) {
            throw new TPKException(String.format(TPKErrorMessage.CANNOT_FIND_BUILD_DIRECTORY, buildDir));
        }
        String binFilePath = PathUtil.addPath(buildDir, binFileName);
        if (!FileUtil.existsPath(binFilePath)) {
            throw new TPKException(String.format(TPKErrorMessage.CANNOT_FIND_BINARY_FILE, binFilePath));
        }
    }

    public Map<String, String> getIncludeListForPackaging(String buildDir, String binFileName, Map<String, String> userIncludeList, List<String> userExcludeList, boolean needEmptyDirectory) throws TPKException {
        TPKSystem tSystem = new TPKSystem();
        return tSystem.getInstallList(buildDir, binFileName, userIncludeList, userExcludeList, needEmptyDirectory);
    }

    public Map<String, String> getIncludeListForPackaging(String buildDir, String binFileName, Map<String, String> userIncludeList, List<String> userExcludeList) throws TPKException {
        return this.getIncludeListForPackaging(buildDir, binFileName, userIncludeList, userExcludeList, false);
    }

    public boolean containsInIncludeList(String srcPath, String buildDir, String binFileName) throws TPKException {
        Map<String, String> includeList = this.getIncludeListForPackaging(buildDir, binFileName, null, null);
        return includeList.keySet().contains(srcPath);
    }

    public String getIncludePath(String relativePath, String buildType, String binFileName) {
        return TPKSystem.getIncludePath(relativePath, buildType, binFileName);
    }

    public synchronized boolean signing(String projectPath, String pathToSign, String targetPath, String profilePath, String profileName, ISigner signer) throws TPKException {
        if (Performance.doPerformance) {
            Performance.setStartTime("Signing time");
        }
        TPKSystem tSystem = new TPKSystem();
        if (this.outStream != null) {
            tSystem.setOutputStream(this.outStream);
        }
        if (this.errorStream != null) {
            tSystem.setErrorStream(this.errorStream);
        }
        tSystem.signing(true, projectPath, pathToSign, targetPath, profilePath, profileName, signer);
        if (Performance.doPerformance) {
            Performance.setEndTime("Signing time");
            log.info(Performance.getAllData().toString());
        }
        return true;
    }

    public synchronized boolean install(String buildDir, String arch, String pkgName, String prjName, String artifactName, String pkgVersion, String stripper, boolean strIP, boolean llvmIR, List<String> excludeList, Map<String, String> includeList) {
        if (Performance.doPerformance) {
            Performance.setStartTime("Installation time");
        }
        TPKSystem tSystem = new TPKSystem();
        if (this.outStream != null) {
            tSystem.setOutputStream(this.outStream);
        }
        if (this.errorStream != null) {
            tSystem.setErrorStream(this.errorStream);
        }
        boolean bRet = false;
        try {
            bRet = tSystem.install(buildDir, arch, pkgName, prjName, artifactName, pkgVersion, stripper, strIP, llvmIR, excludeList, includeList);
        }
        catch (TPKException e) {
            ErrorMessageController.setException(e);
            log.error(e.toString());
            log.exception(e);
            return false;
        }
        log.info("Installation result: " + bRet);
        if (Performance.doPerformance) {
            Performance.setEndTime("Installation time");
            log.info(Performance.getAllData().toString());
        }
        return bRet;
    }

    public synchronized boolean packaging(String buildDir, String pkgName, String pkgVersion, String arch, List<String> excludeList) {
        if (Performance.doPerformance) {
            Performance.setStartTime("Packaging time");
        }
        TPKSystem tSystem = new TPKSystem();
        if (this.outStream != null) {
            tSystem.setOutputStream(this.outStream);
        }
        if (this.errorStream != null) {
            tSystem.setErrorStream(this.errorStream);
        }
        boolean bRet = false;
        try {
            bRet = tSystem.packaging(buildDir, pkgName, pkgVersion, arch, excludeList);
        }
        catch (TPKException e) {
            ErrorMessageController.setException(e);
            log.error(e.toString());
            log.exception(e);
        }
        log.info("Result: " + bRet);
        if (Performance.doPerformance) {
            Performance.setEndTime("Packaging time");
            log.info(Performance.getAllData().toString());
        }
        return bRet;
    }

    public void setOutputStream(OutputStream stream) {
        this.outStream = stream;
    }

    public void setErrorStream(OutputStream stream) {
        this.errorStream = stream;
    }

    public void doPerformance() {
        Performance.doPerformance = true;
    }

    public void endPerformance() {
        Performance.doPerformance = false;
    }

    public String getPerformanceData() {
        StringBuffer buffer = Performance.getAllData();
        return buffer.toString();
    }

    public static Process getCurrentProcess() {
        return CommandRunner.getCurrentProcess();
    }

    public String getErrorMessage() {
        return ErrorMessageController.getErrorMsg();
    }

    public static TPK getInstance() {
        return new TPK();
    }

    public static boolean strip(String projectPath, List<String> resourcePaths, String outputPath, String targetId) {
        if (Performance.doPerformance) {
            Performance.setStartTime("Stripping time");
        }
        TPKSystem tSystem = new TPKSystem();
        if (Performance.doPerformance) {
            Performance.setEndTime("Packaging time");
            log.info(Performance.getAllData().toString());
        }
        return tSystem.stripping(projectPath, resourcePaths, outputPath, targetId);
    }
}

