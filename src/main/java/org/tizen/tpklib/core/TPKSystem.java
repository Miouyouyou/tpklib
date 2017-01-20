/*
* Decompiled with CFR 0_118.
*/
package org.tizen.tpklib.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.tizen.tpklib.TPK;
import org.tizen.tpklib.core.TPKInstaller;
import org.tizen.tpklib.core.TPKPolicy;
import org.tizen.tpklib.core.TPKStripper;
import org.tizen.tpklib.core.project.Project;
import org.tizen.tpklib.core.project.util.ProjectPath;
import org.tizen.tpklib.core.project.util.ProjectUtil;
import org.tizen.tpklib.core.sign.ISigner;
import org.tizen.tpklib.core.util.TPKUtil;
import org.tizen.tpklib.exception.TPKException;
import org.tizen.tpklib.lib.ArgumentValidation;
import org.tizen.tpklib.lib.Command;
import org.tizen.tpklib.lib.CommandRunner;
import org.tizen.tpklib.lib.Log;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.Performance;
import org.tizen.tpklib.lib.Printer;
import org.tizen.tpklib.lib.StringUtil;
import org.tizen.tpklib.lib.error.ErrorMessageController;
import org.tizen.tpklib.lib.file.FileSearch;
import org.tizen.tpklib.lib.file.FileUtil;
import org.tizen.tpklib.lib.zip.IZip;
import org.tizen.tpklib.lib.zip.ZipLibrary;
import org.tizen.tpklib.lib.zip.ZipProvider;
import org.tizen.tpklib.model.BuildOption;
import org.tizen.tpklib.model.PluginManager;
import org.tizen.tpklib.model.Tool;
import org.tizen.tpklib.model.Toolchain;

public class TPKSystem {
	private Log log = TPK.log;
	private OutputStream outStream = null;
	private OutputStream errStream = null;
	private static final String EXTENSION_EDC = "edc";
	private static final String EXTENSION_EDJ = "edj";
	private static final String EXTENSION_MO = "mo";
	private static final String EXTENSION_PO = "po";
	private boolean initialized = false;
	private BuildOption bOpt = null;
	private Project project = null;

	public void initialize(String buildDir, String binFileName, Map<String, String> userIncludeList, List<String> userExcludeList, boolean needEmptyDirectory) throws TPKException {
		this.print("Initialize... ");
		this.bOpt = new BuildOption();
		File buildDirFile = new File(buildDir);
		try {
			this.bOpt.setProjectPath(buildDirFile.getParentFile().getCanonicalPath());
			this.bOpt.setPackageDirPath(PathUtil.addPath(FileUtil.getCanonicalPath(buildDir), ".tpk"));
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
		this.bOpt.setBuildDirectory(buildDir);
		this.bOpt.setbuildType(buildDirFile.getName());
		this.bOpt.setBinFileName(binFileName);
		this.bOpt.setNeedEmptyDirectory(needEmptyDirectory);
		String tepPath = PathUtil.addPath(this.bOpt.getProjectPath(), "res", "tep");
		this.cleanAndVerifyTEPPackage(tepPath);
		this.bOpt.addIncludeMap(this.getInstallList(userIncludeList, userExcludeList));
		this.print("OK\n");
	}

	public void initialize(String buildDir, String binFileName, Map<String, String> userIncludeList, List<String> userExcludeList) throws TPKException {
		this.initialize(buildDir, binFileName, userIncludeList, userExcludeList, false);
	}

	private void cleanAndVerifyTEPPackage(String tepPath) throws TPKException {
		List<String> tepPackageList = TPKUtil.getTEPPackageList(this.bOpt.getBuildDirectory());
		if (FileUtil.existsPath(tepPath)) {
			this.bOpt.setTEPPackaging(true);
			this.bOpt.setTEPDirPath(tepPath);
		}
		String lastModified = this.getTEPHashCode(tepPath);
		for (String tepPackage : tepPackageList) {
			String hashCode = TPKUtil.getTEPHashCodeFromPackageName(PathUtil.getFileNameWithoutExtension(tepPackage));
			if (hashCode != null && hashCode.equals(lastModified)) {
				this.bOpt.setTEPPackaging(false);
				this.bOpt.setTEPDirPath(null);
				continue;
			}
			try {
				FileUtil.removeFile(PathUtil.addPath(this.bOpt.getBuildDirectory(), tepPackage));
				continue;
			}
			catch (IOException e) {
				throw new TPKException(e);
			}
		}
	}

	public Map<String, String> getInstallList(String buildDir, String binFileName, Map<String, String> userIncludeList, List<String> userExcludeList, boolean needEmptyDirectory) throws TPKException {
		this.initialize(buildDir, binFileName, userIncludeList, userExcludeList, needEmptyDirectory);
		TPKPolicy policy = new TPKPolicy(this.bOpt.getNeedEmptyDirectory());
		return policy.getInstallList(this.bOpt.getProjectPath(), this.bOpt.getBuildType(), this.bOpt.getBinFileName(), userIncludeList, userExcludeList, this.bOpt.isTEPPackaging());
	}

	public Map<String, String> getInstallList(String buildDir, String binFileName, Map<String, String> userIncludeList, List<String> userExcludeList) throws TPKException {
		return this.getInstallList(buildDir, binFileName, userIncludeList, userExcludeList, false);
	}

	private Map<String, String> getInstallList(Map<String, String> userIncludeList, List<String> userExcludeList) throws TPKException {
		TPKPolicy policy = new TPKPolicy(this.bOpt.getNeedEmptyDirectory());
		return policy.getInstallList(this.bOpt.getProjectPath(), this.bOpt.getBuildType(), this.bOpt.getBinFileName(), userIncludeList, userExcludeList, this.bOpt.isTEPPackaging());
	}

	public void install() throws TPKException {
		this.print("Copying files... ");
		TPKInstaller.install(this.bOpt);
		this.print("OK\n");
	}

	public void strip(String pkgId, String stripper) throws TPKException {
		if (TPKStripper.isStrip(stripper)) {
			this.print("Stripping... ");
			TPKStripper tpkStripper = new TPKStripper();
			tpkStripper.setOutputStream(this.outStream);
			tpkStripper.setErrorStream(this.errStream);
			tpkStripper.strip(pkgId, this.bOpt.getProjectPath(), this.bOpt.getBuildType(), stripper);
			this.print("OK\n");
		}
	}

	public void signing(boolean isSign, String profilePath, String profileName, ISigner signer) throws TPKException {
		this.signing(isSign, this.bOpt.getProjectPath(), this.bOpt.getPackageDirPath(), this.bOpt.getPackageDirPath(), profilePath, profileName, signer);
	}

	public void signing(boolean isSign, String projectPath, String pathToSign, String targetPath, String profilePath, String profileName, ISigner signer) throws TPKException {
		if (isSign && this.isSign(signer)) {
			this.print("Signing... ");
			this.print(
				String.format("projectPath : %s, pathToSign : %s, targetPath : %s, profilePath : %s, profileName : %s\n",
			                 projectPath, pathToSign, targetPath, profilePath, profileName)
			);
			signer.sign(projectPath, pathToSign, targetPath, profilePath, profileName);
			this.print("OK\n");
		}
	}

	public boolean packaging(String buildDir, String pkgName, String pkgVersion, String arch) throws TPKException {
		this.println(
			String.format("buildDir: %s, pkgName: %s, pkgVersion: %s, arch: %s\n",
		                 buildDir, pkgName, pkgVersion, arch)
		);

		if (this.bOpt != null && this.bOpt.isTEPPackaging()) {
			this.packageTEP(buildDir, pkgName);
		}
		this.cleanTPKPackageFile(buildDir, pkgName, pkgVersion, arch);
		String packagePath = this.getPackagePath(buildDir, pkgName, pkgVersion, arch);
		this.println("Zip pouf: " + packagePath);
		if (PathUtil.existsFile(packagePath)) {
			try {
				Command.remove(packagePath);
			}
			catch (IOException e) {
				throw new TPKException(e);
			}
			catch (InterruptedException e) {
				throw new TPKException(e);
			}
		}
		IZip zip = ZipProvider.getZipInstance();
		try {
			if (this.outStream != null) {
				zip.setOutputStream(this.outStream);
			}
			if (zip.zipping(new File(PathUtil.addPath(buildDir, ".tpk")), packagePath, 493)) {
				this.print("Zipping... OK\n");
				return true;
			}
			throw new TPKException(String.format("Cannot create package(%s)", packagePath));
		}
		catch (FileNotFoundException e) {
			throw new TPKException(e);
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
		catch (InterruptedException e) {
			throw new TPKException(e);
		}
	}

	public static String getIncludePath(String srcPath, String buildType, String binFileName) {
		if (!ArgumentValidation.validateStringArgument(srcPath, buildType, binFileName)) {
			return null;
		}
		String srcOSPath = FileUtil.getOSPath(srcPath);
		Map<String, String> defaultIncludeList = TPKPolicy.getDefaultIncludeList(buildType, binFileName);
		String targetPath = null;
		for (String defaultPath : defaultIncludeList.keySet()) {
			if (!srcOSPath.startsWith(defaultPath)) continue;
			targetPath = srcOSPath.replace(defaultPath, defaultIncludeList.get(defaultPath));
			break;
		}
		if (targetPath == null || targetPath.isEmpty()) {
			return null;
		}
		for (String defaultExcludePath : TPKPolicy.getDefaultExcludeList(null)) {
			Pattern pattern = Pattern.compile(PathUtil.pathToRegularExpression(defaultExcludePath));
			Matcher matcher = pattern.matcher(srcOSPath);
			if (!matcher.matches()) continue;
			targetPath = null;
			break;
		}
		return targetPath;
	}

	public boolean cleanTemporaryFiles(String buildDir) {
		try {
			BuildOption.getInstance().clear();
			FileUtil.removeFile(PathUtil.addPath(FileUtil.getCanonicalPath(buildDir), ".tpk"));
			FileUtil.removeFile(PathUtil.addPath(buildDir, ".packaging"));
			return true;
		}
		catch (IOException e) {
			this.log.exception(e);
			return false;
		}
	}

	public void cleanAll(String buildDir, String pkgName, String appVersion, String arch) throws TPKException {
		this.cleanTemporaryFiles(buildDir);
		try {
			FileUtil.removeFile(this.getPackagePath(buildDir, pkgName, appVersion, arch));
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
	}

	public synchronized boolean initialize(String buildDir, String arch, String pkgName, String prjName, String artifactName, String pkgVersion) throws TPKException {
		this.print("Initialize... ");
		if (Performance.doPerformance) {
			Performance.setStartTime("Initialize");
		}
		try {
			this.project = new Project(FileUtil.getParentDirectory(buildDir));
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
		if (FileUtil.existsPath(ProjectPath.getTEPPath(this.project))) {
			BuildOption.getInstance().setTEPPackaging(true);
		} else {
			BuildOption.getInstance().setTEPPackaging(false);
		}
		if (!ArgumentValidation.validateStringArgument(buildDir, arch, pkgName, prjName, artifactName, pkgVersion)) {
			String errMsg = "Check your option => " + ArgumentValidation.getArguments(buildDir, arch, pkgName, prjName, artifactName, pkgVersion);
			this.setErrMsg(errMsg);
			this.print("failed!\n");
			return false;
		}
		if (!this.cleanPackagingFiles(buildDir, pkgName, pkgVersion, arch)) {
			this.log.error("Cannot clean the build directory.");
			this.print("failed!\n");
			return false;
		}
		if (Performance.doPerformance) {
			Performance.setEndTime("Initialize");
		}
		this.print("OK\n");
		this.initialized = true;
		return true;
	}

	public synchronized void initialize(String buildDir, String arch, String pkgName, String prjName, String artifactName, String pkgVersion, String stripper, boolean strIP, boolean llvmIR, ISigner signer, List<String> excludeList, Map<String, String> includeList) throws TPKException {
		this.print("Initialize... ");
		if (Performance.doPerformance) {
			Performance.setStartTime("Initialize");
		}
		this.project = new Project(ProjectUtil.getProjectLocation(buildDir), ProjectUtil.getConfigurationName(buildDir), ProjectUtil.getProjectTypeName(artifactName));
		if (this.bOpt == null) {
			this.bOpt = new BuildOption();
		} else {
			this.bOpt.clear();
		}
		if (FileUtil.existsPath(ProjectPath.getTEPPath(this.project))) {
			this.bOpt.setTEPPackaging(true);
		} else {
			this.bOpt.setTEPPackaging(false);
		}
		if (!ArgumentValidation.validateStringArgument(buildDir, arch, pkgName, prjName, artifactName, pkgVersion)) {
			String errMsg = "Check your options => " + ArgumentValidation.getArguments(buildDir, arch, pkgName, prjName, artifactName, pkgVersion);
			throw new TPKException(errMsg);
		}
		this.bOpt.setBuildDirectory(buildDir);
		this.bOpt.setArch(arch);
		this.bOpt.setPkgName(pkgName);
		this.bOpt.setPrjName(prjName);
		this.bOpt.setArtifactName(artifactName);
		this.bOpt.setPkgVersion(pkgVersion);
		this.bOpt.setSigner(signer);
		this.bOpt.setStripper(stripper);
		this.bOpt.setStrIP(strIP);
		this.bOpt.setLlvmIR(llvmIR);
		this.initIncludeMap(includeList);
		this.initExcludeList(excludeList);
		if (!this.cleanPackagingFiles(buildDir, pkgName, pkgVersion, arch)) {
			throw new TPKException("Cannot clean the build directory.");
		}
		if (Performance.doPerformance) {
			Performance.setEndTime("Initialize");
		}
		this.print("OK\n");
		this.initialized = true;
	}

	private void initIncludeMap(Map<String, String> incList) throws TPKException {
		this.bOpt.addIncludeMap(this.getPackagingFileList(TPKPolicy.getDefaultPackagingMap(ProjectUtil.getBuildDirName(this.project), ProjectUtil.getBinDirName(this.project), this.bOpt.getArtifactName())));
		this.bOpt.addIncludeMap(this.getPackagingFileList(incList));
	}

	private void initExcludeList(List<String> excList) throws TPKException {
		for (String excPath2 : TPKPolicy.getDefaultExcludeList()) {
			this.excludeFileFromIncludeList(PathUtil.addPath(this.project.getLocation(), excPath2));
		}
		for (String excPath2 : excList) {
			this.excludeFileFromIncludeList(PathUtil.addPath(this.project.getLocation(), excPath2));
		}
	}

	private void excludeFileFromIncludeList(String filePath) throws TPKException {
		this.log.log("Exclude path => " + filePath);
		if (filePath == null || filePath.isEmpty()) {
			return;
		}
		List<String> excludeList = null;
		try {
			excludeList = FileSearch.getFileList(filePath);
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
		for (String excludeFile : excludeList) {
			this.bOpt.getIncludeList().remove(excludeFile);
		}
	}

	private Map<String, String> getPackagingFileList(Map<String, String> srcList) throws TPKException {
		HashMap<String, String> pkgFileList = new HashMap<String, String>();
		Set<String> srcKeySet = srcList.keySet();
		for (String srcPath : srcKeySet) {
			List<String> fileList;
			String fullPath = PathUtil.addPath(this.project.getLocation(), srcPath);
			if (!FileUtil.existsPath(fullPath)) continue;
			try {
				fileList = FileSearch.getFileList(fullPath);
			}
			catch (IOException e) {
				throw new TPKException(e);
			}
			for (String file : fileList) {
				String pkgPath = file.replace(fullPath, srcList.get(srcPath));
				pkgFileList.put(file, pkgPath);
			}
		}
		return pkgFileList;
	}

	public boolean initialize(BuildOption bOption) throws TPKException {
		this.print("Initialize... ");
		if (Performance.doPerformance) {
			Performance.setStartTime("Initialize");
		}
		if (bOption == null) {
			throw new TPKException(String.format("Invalid option\n", bOption));
		}
		this.project = new Project(ProjectUtil.getProjectLocation(bOption.getBuildDirectory()), ProjectUtil.getConfigurationName(bOption.getBuildDirectory()), ProjectUtil.getProjectTypeName(bOption.getArtifactName()));
		this.bOpt = bOption;
		if (!ArgumentValidation.validateStringArgument(bOption.getBuildDirectory(), bOption.getArch(), bOption.getPkgName(), bOption.getPkgVersion())) {
			throw new TPKException(String.format("Invalid option\n", bOption));
		}
		if (FileUtil.existsPath(ProjectPath.getTEPPath(bOption.getBuildDirectory()))) {
			BuildOption.getInstance().setTEPPackaging(true);
		} else {
			BuildOption.getInstance().setTEPPackaging(false);
		}
		if (!this.cleanPackagingFiles(bOption.getBuildDirectory(), bOption.getPkgName(), bOption.getPkgVersion(), bOption.getArch())) {
			throw new TPKException("Cannot clean the build directory.");
		}
		if (Performance.doPerformance) {
			Performance.setEndTime("Initialize");
		}
		this.print("OK\n");
		this.initialized = true;
		return true;
	}

	private boolean cleanPackageFiles(String buildDir, String pkgName, String pkgVersion, String arch) throws TPKException {
		if (!this.cleanTPKPackageFile(buildDir, pkgName, pkgVersion, arch)) {
			this.log.error("Cannot remove the tpk file.");
		}
		return true;
	}

	private boolean cleanPackagingFiles(String buildDir, String pkgName, String pkgVersion, String arch) throws TPKException {
		if (!this.cleanTPKBuildDirectory(buildDir)) {
			this.log.error("Cannot clean the " + buildDir);
		}
		if (!this.cleanPackageFiles(buildDir, pkgName, pkgVersion, arch)) {
			this.log.error("Cannot remove package file.");
		}
		return true;
	}

	private boolean cleanTPKBuildDirectory(String buildDir) throws TPKException {
		String tpkPath = PathUtil.addPath(buildDir, ".tpk");
		if (new File(tpkPath).exists()) {
			try {
				if (Command.rm(tpkPath)) {
					return true;
				}
				String errMsg = String.format("Cannot remove \"%s\"", tpkPath);
				throw new TPKException(errMsg);
			}
			catch (IOException e) {
				throw new TPKException(e);
			}
			catch (InterruptedException e) {
				throw new TPKException(e);
			}
		}
		return true;
	}

	private boolean cleanTPKPackageFile(String buildDir, String pkgName, String pkgVersion, String arch) throws TPKException {
		String tpkFilePath = PathUtil.addPath(buildDir, pkgName + "-" + pkgVersion + "-" + arch);
		String tpkPath = tpkFilePath + ".tpk";
		String tpkDebugPath = tpkFilePath + "-debug.zip";
		try {
			if (FileUtil.existsPath(tpkPath) && !Command.rm(tpkPath)) {
				throw new TPKException(String.format("Cannot remove \"%s\"", tpkPath));
			}
			if (FileUtil.existsPath(tpkDebugPath) && !Command.rm(tpkDebugPath)) {
				throw new TPKException(String.format("Cannot remove \"%s\"", tpkDebugPath));
			}
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
		catch (InterruptedException e) {
			throw new TPKException(e);
		}
		return true;
	}

	public boolean install(BuildOption bOption) throws TPKException {
		if (bOption == null) {
			throw new TPKException("Invalid argument\n" + bOption);
		}
		if (!this.initialized) {
			this.initialize(bOption);
		}
		this.install(bOption.getBuildDirectory(), bOption.getIncludeList(), bOption.getExcludeList());
		this.stripping(bOption);
		this.installLLVMBitcode();
		if (bOption.isLlvmIR()) {
			this.print("\nCopying LLVM bitcode... ");
			if (!this.installLLVMBitcode()) {
				this.print("failed!\n");
				this.log.warn("Bitcode fail.");
			} else {
				this.print("OK\n");
			}
		}
		if (Performance.doPerformance) {
			Performance.setEndTime("Installation");
		}
		if (bOption.isStrIP() || bOption.isLlvmIR()) {
			this.print("Copying files... OK\n");
		} else {
			this.print("OK\n");
		}
		return true;
	}

	public boolean install(String buildDir, Map<String, String> includeMap, List<String> excludeList) throws TPKException {
		this.print("Copying files... ");
		if (includeMap == null || includeMap.isEmpty()) {
			includeMap = this.getIncludeMapFromBuildDirectory();
		}
		TPKInstaller installer = new TPKInstaller();
		installer.install(buildDir, includeMap, excludeList);
		this.print("OK\n");
		return true;
	}

	private Map<String, String> getIncludeMapFromBuildDirectory() throws TPKException {
		Map<String, String> includeMap = this.getPackagingFileList(TPKPolicy.getDefaultPackagingMap(ProjectUtil.getBuildDirName(this.project), ProjectUtil.getBinDirName(this.project), this.bOpt.getArtifactName()));
		for (String excPath : TPKPolicy.getDefaultExcludeList()) {
			this.excludeFileFromIncludeList(PathUtil.addPath(this.project.getLocation(), excPath));
		}
		return includeMap;
	}

	private void installExceptionsDirectory(String packagingDir) throws TPKException {
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

	public synchronized boolean install(String buildDir, String arch, String pkgName, String prjName, String artifactName, String pkgVersion, String stripper, boolean strIP, boolean llvmIR, List<String> excludeList, Map<String, String> includeList) throws TPKException {
		if (!this.initialized && !this.initialize(buildDir, arch, pkgName, prjName, artifactName, pkgVersion)) {
			this.log.error("Cannot initialize.");
			return false;
		}
		this.print("Copying files... ");
		if (Performance.doPerformance) {
			Performance.setStartTime("Installation");
		}
		if (!this.installBuildDirectory(buildDir)) {
			if (Performance.doPerformance) {
				Performance.setEndTime("Installation");
			}
			this.log.error("Package install fail.");
			this.print("failed!\n");
			return false;
		}
		if (!this.installBuildFile(buildDir, artifactName)) {
			if (Performance.doPerformance) {
				Performance.setEndTime("Installation");
			}
			this.log.error("Package install fail.");
			this.print("failed!\n");
			return false;
		}
		if (this.existEFLFile(buildDir) && !this.installEfl(buildDir)) {
			if (Performance.doPerformance) {
				Performance.setEndTime("Installation");
			}
			this.log.error("Package install fail.");
			this.print("failed!\n");
			return false;
		}
		if (this.existLocaleFile(buildDir) && !this.installMOFile(buildDir, artifactName)) {
			if (Performance.doPerformance) {
				Performance.setEndTime("Installation");
			}
			this.log.error("Package install fail.");
			this.print("failed!\n");
			return false;
		}
		if (BuildOption.getInstance().isTEPPackaging()) {
			this.removeTEP(buildDir);
		}
		if (this.isMultiApp(buildDir)) {
			if (!this.installMultiApp(buildDir, artifactName)) {
				if (Performance.doPerformance) {
					Performance.setEndTime("Installation");
				}
				this.log.error("Package install fail.");
				this.print("failed!\n");
				return false;
			}
		} else if (!this.installConfiguration(buildDir, artifactName)) {
			if (Performance.doPerformance) {
				Performance.setEndTime("Installation");
			}
			this.log.error("Package install fail.");
			this.print("failed!\n");
			return false;
		}
		if (!this.cleanUpBuildDir(buildDir, excludeList, includeList)) {
			this.log.warn("Cannot Make up build directory.");
			this.log.warn("exclude list => " + excludeList.toString());
			this.log.warn("include list => " + includeList.toString());
		}
		if (strIP) {
			this.print("\nStripping... ");
			this.stripping(buildDir, pkgName, stripper);
			this.print("OK\n");
		}
		if (llvmIR) {
			this.print("\nCopying LLVM bitcode... ");
			if (!this.installLLVMBitcode()) {
				this.print("failed!\n");
				this.log.warn("Bitcode fail.");
			} else {
				this.print("OK\n");
			}
		}
		if (Performance.doPerformance) {
			Performance.setEndTime("Installation");
		}
		if (strIP || llvmIR) {
			this.print("Copying files... OK\n");
		} else {
			this.print("OK\n");
		}
		return true;
	}

	private void removeTEP(String buildDir) throws TPKException {
		String tepPackagingDir = PathUtil.addPath(buildDir, ".tpk", "res", "tep");
		if (FileUtil.existsPath(tepPackagingDir)) {
			try {
				FileUtil.removeFileR(tepPackagingDir);
			}
			catch (IOException e) {
				throw new TPKException(e);
			}
		}
	}

	private boolean cleanUpBuildDir(String buildDir, List<String> excludeList, Map<String, String> includeList) throws TPKException {
		String resPath;
		boolean bRet = true;
		if (excludeList != null) {
			for (String excludeFile : excludeList) {
				String excludeFilePath = PathUtil.addPath(buildDir, ".tpk", excludeFile);
				if (!new File(excludeFilePath).exists()) continue;
				try {
					if (Command.rm(excludeFilePath)) continue;
					this.log.warn("Cannot remove " + excludeFilePath);
					bRet = false;
				}
				catch (IOException e) {
					this.log.exception(e);
					bRet = false;
				}
				catch (InterruptedException e) {
					this.log.exception(e);
					bRet = false;
				}
			}
		}
		if (!FileUtil.removeFileR(resPath = PathUtil.addPath(buildDir, ".tpk", "res"), "mk")) {
			this.log.warn("Cannot remove mk File");
		}
		this.installExceptionsDirectory(PathUtil.addPath(buildDir, ".tpk"));
		return bRet;
	}

	private boolean installBuildDirectory(String buildDir) throws TPKException {
		String projectDir;
		if (!this.createBuildDir(buildDir)) {
			this.log.error("Cannot create build directory => " + buildDir);
			return false;
		}
		if (!this.installDirectory("shared/res", "shared/res", buildDir, true)) {
			this.log.error("Cannot install the directory. => shared/res, shared/res");
			return false;
		}
		if (!this.installDirectory("icons", "shared/res", buildDir, true)) {
			this.log.error("Cannot install the directory. => icons, shared/res");
			return false;
		}
		if (!this.installDirectory("shared/trusted", "shared/trusted", buildDir, true)) {
			this.log.error("Cannot install the directory. => shared/trusted, shared/trusted");
			return false;
		}
		if (!this.installDirectory("lib", "lib", buildDir, true)) {
			this.log.error("Cannot install the directory. => lib, lib");
			return false;
		}
		try {
			projectDir = PathUtil.getParentDirectory(buildDir);
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
		String srcPath = PathUtil.addPath(projectDir, "res");
		String dstPath = PathUtil.addPath(buildDir, ".tpk");
		try {
			this.log.info(String.format("Copy from \"%s\" to \"%s\"", srcPath, dstPath));
			if (!Command.copy(srcPath, dstPath, "-rf")) {
				this.log.warn("Cannot install directory.(" + srcPath + ")");
			}
		}
		catch (IOException e) {
			this.log.exception(e);
		}
		catch (InterruptedException e) {
			this.log.exception(e);
		}
		if (!this.installDirectory("bin", "bin", buildDir, true)) {
			this.log.error("Cannot install the directory. => bin, bin");
			return false;
		}
		if (!this.installDirectory("Debug/res", "", buildDir, true)) {
			this.log.error("Cannot install the directory. => bin, bin");
			return false;
		}
		return true;
	}

	private boolean installBuildFile(String buildDir, String artifactName) {
		String targetPath = PathUtil.addPath("bin", artifactName);
		if (!this.installFile(artifactName, targetPath, buildDir)) {
			this.log.error("Cannot install the file." + artifactName);
			return false;
		}
		return true;
	}

	private boolean existLocaleFile(String buildDir) {
		String eflResPath = PathUtil.addPath(buildDir, "res");
		try {
			return PathUtil.existExtensionInPath(eflResPath, "mo");
		}
		catch (IOException e) {
			this.log.exception(e);
			return false;
		}
	}

	private boolean existEFLFile(String buildDir) {
		String eflResPath = PathUtil.addPath(buildDir, "res");
		try {
			return PathUtil.existExtensionInPath(eflResPath, "edj");
		}
		catch (IOException e) {
			this.log.exception(e);
			return false;
		}
	}

	private boolean installEfl(String buildDir) {
		String srcPath = PathUtil.addPath(buildDir, "res");
		String dstPath = PathUtil.addPath(buildDir, ".tpk");
		try {
			this.log.info(String.format("Copy from \"%s\" to \"%s\"", srcPath, dstPath));
			if (!Command.copy(srcPath, dstPath, "-rf")) {
				this.setErrMsg(String.format("Cannot copy from \"%s\" to \"%s\"", srcPath, dstPath));
				return false;
			}
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
		String resPath = PathUtil.addPath(buildDir, ".tpk", "res");
		if (!FileUtil.removeFileR(resPath, "edc")) {
			this.log.warn("Cannot remove edc File");
		}
		return true;
	}

	private boolean installMOFile(String buildDir, String artifactName) {
		this.log.info("##### Install MO file #####");
		String poDir = PathUtil.addPath(buildDir, ".tpk", "res", "po");
		File poDirFile = new File(poDir);
		if (!poDirFile.exists()) {
			return true;
		}
		for (File childFile : poDirFile.listFiles()) {
			if (childFile == null || !PathUtil.getExtension(childFile.getAbsolutePath()).equalsIgnoreCase("mo") || this.moveMoFileToLocale(buildDir, artifactName, childFile.getAbsolutePath())) continue;
			return false;
		}
		if (this.removeUnnecessaryPOFile(buildDir)) {
			this.log.warn("Remove unnecessary po file failed");
		}
		return true;
	}

	private boolean removeUnnecessaryPOFile(String buildDir) {
		boolean bRet = true;
		String poDir = PathUtil.addPath(buildDir, ".tpk", "res", "po");
		File poDirFile = new File(poDir);
		try {
			String resPath = PathUtil.addPath(buildDir, ".tpk", "res");
			if (!FileUtil.removeFileR(resPath, "po")) {
				this.log.warn("Cannot remove .po File");
				bRet = false;
			}
			this.log.info("remove mo file except \"locale\"");
			ArrayList<String> removeExtensionList = new ArrayList<String>();
			removeExtensionList.add("mo");
			ArrayList<String> excludeDirList = new ArrayList<String>();
			excludeDirList.add(PathUtil.addPath(resPath, "locale"));
			if (!FileUtil.removeFileR(new File(resPath), removeExtensionList, excludeDirList)) {
				this.log.warn("Cannot remove mo File");
				bRet = false;
			}
			if (!(poDirFile.listFiles() != null && poDirFile.listFiles().length > 0 || Command.rm(poDirFile.getAbsolutePath()))) {
				this.log.error("Cannot remove directory => " + poDirFile.getAbsolutePath());
				bRet = false;
			}
		}
		catch (IOException e) {
			this.log.exception(e);
			bRet = false;
		}
		catch (InterruptedException e) {
			this.log.exception(e);
			bRet = false;
		}
		return bRet;
	}

	private boolean moveMoFileToLocale(String buildDir, String artifactName, String moFilePath) {
		String fileNameWithoutExtension;
		String localeDir = PathUtil.addPath(buildDir, ".tpk", "res", "locale");
		String outputName = PathUtil.addPath(localeDir, fileNameWithoutExtension = PathUtil.getFileNameWithoutExtension(moFilePath), "LC_MESSAGES", artifactName + "." + "mo");
		File dstFile = new File(outputName);
		if (!PathUtil.existParentDirectory(dstFile) && !dstFile.getParentFile().mkdirs()) {
			this.setErrMsg(String.format("Cannot create directory(%s)", dstFile.getParent()));
			return false;
		}
		File srcMoFile = new File(moFilePath);
		this.log.info(String.format("Move mo files from \"%s\" to \"%s\"", srcMoFile.getAbsolutePath(), dstFile.getAbsolutePath()));
		if (!srcMoFile.renameTo(dstFile)) {
			try {
				this.setErrMsg(String.format("Cannot move from \"%s\" to \"%s\"", srcMoFile.getCanonicalPath(), dstFile.getCanonicalPath()));
			}
			catch (IOException e) {
				this.setErrMsg(e);
			}
			return false;
		}
		return true;
	}

	private boolean createBuildDir(String buildDir) {
		String tpkSharedDir = PathUtil.addPath(buildDir, ".tpk", "shared");
		try {
			if (!Command.mkdir(tpkSharedDir)) {
				this.setErrMsg("Cannot create build directory => " + tpkSharedDir);
				return false;
			}
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
		return true;
	}

	private boolean installDirectory(String src, String dst, String buildDir, boolean inclusion) {
		String srcPath = PathUtil.addPath(buildDir, "..", src);
		String dstPath = PathUtil.addPath(buildDir, ".tpk", dst);
		this.log.info("Install directory from \"" + srcPath + "\" to " + "\"" + dstPath + "\"");
		File srcFile = new File(srcPath);
		try {
			if (srcFile.exists()) {
				if (Command.cp(srcPath, dstPath)) {
					return true;
				}
				this.setErrMsg(String.format("Cannot copy from \"%s\" to \"%s\"", srcPath, dstPath));
				return false;
			}
			if (inclusion) {
				File dstFile = new File(dstPath);
				if (!dstFile.exists()) {
					if (Command.mkdir(dstPath)) {
						return true;
					}
					this.setErrMsg(String.format("Cannot create directory(%s)", dstPath));
					return false;
				}
				return true;
			}
			return true;
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
	}

	private boolean installFile(String src, String dst, String buildDir) {
		String srcPath = PathUtil.addPath(buildDir, src);
		String dstPath = PathUtil.addPath(buildDir, ".tpk", dst);
		this.log.info("Install directory from \"" + srcPath + "\" to " + "\"" + dstPath + "\"");
		if (!PathUtil.existsFile(srcPath)) {
			this.setErrMsg(String.format("\n -> Cannot find the target file(%s).\n", PathUtil.getFileName(srcPath)));
			return false;
		}
		try {
			if (Command.cp(srcPath, dstPath)) {
				return true;
			}
			this.setErrMsg(String.format("Cannot copy from \"%s\" to \"%s\"", srcPath, dstPath));
			return false;
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
	}

	public boolean stripping(String projectPath, List<String> resourcePaths, String outputPath, String targetId) {
		Toolchain tc;
		PluginManager pm;
		this.log.info(String.format("stripping(%s, %s, %s, %s)", projectPath, resourcePaths, outputPath, targetId));
		if (projectPath == null || projectPath.isEmpty()) {
			this.log.error("Project path is empty");
			return false;
		}
		if (resourcePaths.size() <= 0) {
			this.log.info("Resources is empty");
			return true;
		}
		if (outputPath == null || outputPath.isEmpty()) {
			this.log.error("Output path is empty");
		}
		if (targetId == null || targetId.isEmpty()) {
			this.log.error("Target is empty");
		}
		if ((tc = (pm = PluginManager.getInstance()).getToolchainByTargetId(targetId)) == null) {
			this.log.error("Cannot find toolchain from target id => " + targetId);
			return false;
		}
		if (!pm.isArmArchitecture(tc.getArchitecture())) {
			this.log.warn("Cannot stripping. Architecture is " + tc.getArchitecture());
			return false;
		}
		List<String> outputFilePaths = this.copyToOutputPath(projectPath, resourcePaths, outputPath);
		Tool stripper = tc.getStrip();
		if (stripper == null) {
			this.log.warn("Stripper doesn't exist");
			return false;
		}
		boolean ret = true;
		for (String outputFilePath : outputFilePaths) {
			if (this.stripBinary(outputFilePath, stripper.getPath())) continue;
			this.log.warn("Stripping failed");
			ret = false;
		}
		return ret;
	}

	private List<String> copyToOutputPath(String projectPath, List<String> resourcePaths, String outputPath) {
		ArrayList<String> retResourcePaths = new ArrayList<String>();
		for (String resourcePath : resourcePaths) {
			String srcFilePath = PathUtil.addPath(projectPath, resourcePath);
			if (!PathUtil.existsFile(srcFilePath)) {
				this.log.warn("Cannot find file => " + srcFilePath);
				continue;
			}
			String dstFilePath = PathUtil.addPath(projectPath, PathUtil.addPath(outputPath, resourcePath));
			File dstFile = new File(dstFilePath);
			File dstParent = dstFile.getParentFile();
			if (dstFile.exists()) {
				dstFile = dstFile.getParentFile();
			} else if (!dstParent.exists()) {
				dstParent.mkdirs();
			}
			try {
				File srcFile = new File(srcFilePath);
				if (!Command.cp(srcFile.getCanonicalPath(), dstFile.getCanonicalPath())) {
					this.log.warn(String.format("Cannot copy from %s to %s", srcFilePath, dstFile.getCanonicalPath()));
					continue;
				}
				if (dstFile.isDirectory()) {
					List<String> fileList = PathUtil.getChildFilePathList(dstFile);
					retResourcePaths.addAll(fileList);
					continue;
				}
				retResourcePaths.add(dstFile.getCanonicalPath());
			}
			catch (IOException e) {
				this.log.exception(e);
			}
			catch (InterruptedException e) {
				this.log.exception(e);
			}
		}
		return retResourcePaths;
	}

	private boolean stripping(BuildOption bOption) throws TPKException {
		if (bOption == null) {
			throw new TPKException("Invalid argument\n" + bOption);
		}
		if (bOption.isStrIP()) {
			this.stripping(bOption.getBuildDirectory(), bOption.getPkgName(), bOption.getStripper());
		}
		return true;
	}

	private boolean stripping(String buildDir, String pkgName, String stripper) throws TPKException {
		this.print("Stripping... ");
		if (stripper == null || stripper.isEmpty()) {
			throw new TPKException("Cannot find strip tool => " + stripper);
		}
		String debugInfoDirPath = PathUtil.addPath(buildDir, ".debuginfo");
		if (new File(debugInfoDirPath).exists()) {
			try {
				if (!Command.rm(debugInfoDirPath)) {
					throw new TPKException(String.format("Cannot remove directory(%s)", debugInfoDirPath));
				}
			}
			catch (IOException e) {
				throw new TPKException(e);
			}
			catch (InterruptedException e) {
				throw new TPKException(e);
			}
		}
		String debugInfoAppsDirPath = PathUtil.addPath(debugInfoDirPath, "usr", "lib", "debug", "opt", "usr", "apps");
		try {
			if (!Command.mkdir(debugInfoAppsDirPath)) {
				throw new TPKException(String.format("Cannot create directory(%s)", debugInfoAppsDirPath));
			}
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
		catch (InterruptedException e) {
			throw new TPKException(e);
		}
		for (String stripDir : TPKPolicy.TPK_STRIP_DIR_LIST) {
			if (this.stripDirectory(buildDir, debugInfoAppsDirPath, pkgName, stripDir, stripper)) continue;
			throw new TPKException("Cannot strip directory => " + stripDir);
		}
		this.print("OK\n");
		return true;
	}

	private boolean stripDirectory(String buildDir, String debugInfoAppsDirPath, String pkgName, String targetDir, String stripper) throws TPKException {
		String debugInfoDir = PathUtil.addPath(debugInfoAppsDirPath, pkgName, targetDir);
		try {
			if (!Command.mkdir(debugInfoDir)) {
				throw new TPKException(String.format("Cannot create directory(%s)", debugInfoDir));
			}
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
		catch (InterruptedException e) {
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
			ArrayList<String> cmd = new ArrayList<String>();
			cmd.add(stripper);
			cmd.add("--only-keep-debug");
			cmd.add("-o");
			cmd.add(debugInfoFile);
			cmd.add(file);
			this.log.log("Strip command => " + cmd);
			CommandRunner debugCR = null;
			try {
				debugCR = Command.execute(cmd, null, null, false);
				this.printCommandOutput(debugCR);
				if (debugCR == null) {
					throw new TPKException(String.format("Cannot strip file(%s)", file));
				}
				debugCR.waitFor();
			}
			catch (IOException e) {
				throw new TPKException(e);
			}
			catch (InterruptedException e) {
				throw new TPKException(e);
			}
			this.log.log("exit value of strip command => " + debugCR.getExitValue());
			ArrayList<String> stripCommand = new ArrayList<String>();
			stripCommand.add(stripper);
			stripCommand.add("--strip-unneeded");
			stripCommand.add("--remove-section");
			stripCommand.add(".comment");
			stripCommand.add(file);
			this.log.log("Strip command => " + stripCommand);
			CommandRunner stripCR = null;
			try {
				stripCR = Command.execute(stripCommand, null, null, false);
				this.printCommandOutput(stripCR);
				if (stripCR == null) {
					throw new TPKException(String.format("Cannot strip file(%s)", file));
				}
				stripCR.waitFor();
			}
			catch (IOException e) {
				throw new TPKException(e);
			}
			catch (InterruptedException e) {
				throw new TPKException(e);
			}
			this.log.log("exit value of strip command => " + stripCR.getExitValue());
		}
		return true;
	}

	public boolean stripBinary(String filePath, String stripper) {
		CommandRunner stripCR;
		block7 : {
			if (filePath == null || filePath.isEmpty() || !PathUtil.existsFile(filePath)) {
				this.log.error("File do not exist => " + filePath);
				return false;
			}
			if (stripper == null || stripper.isEmpty() || !PathUtil.existsFile(stripper)) {
				this.log.error("Stripper do not exist => " + stripper);
				return false;
			}
			ArrayList<String> stripCommand = new ArrayList<String>();
			stripCommand.add(stripper);
			stripCommand.add("--strip-unneeded");
			stripCommand.add("--remove-section");
			stripCommand.add(".comment");
			stripCommand.add(filePath);
			this.log.log("Strip command => " + stripCommand);
			stripCR = null;
			try {
				stripCR = Command.execute(stripCommand, null, null, false);
				this.printCommandOutput(stripCR);
				if (stripCR != null) {
					stripCR.waitFor();
					break block7;
				}
				return false;
			}
			catch (IOException e) {
				this.log.exception(e);
				return false;
			}
			catch (InterruptedException e) {
				this.log.exception(e);
				return false;
			}
		}
		int retValue = stripCR.getExitValue();
		this.log.log("exit value of strip command => " + retValue);
		if (retValue != 0) {
			return false;
		}
		return true;
	}

	private boolean installLLVMBitcode() {
		return false;
	}

	private void printCommandOutput(CommandRunner cr) {
		Thread outputThread = this.print(cr.getInputStream(), this.outStream);
		Thread errorThread = this.print(cr.getErrorStream(), this.errStream);
		try {
			outputThread.join();
		}
		catch (InterruptedException e) {
			this.log.exception(e);
		}
		try {
			errorThread.join();
		}
		catch (InterruptedException e) {
			this.log.exception(e);
		}
	}

	private Thread print(final InputStream input, final OutputStream output) {
		Thread printThread = new Thread(new Runnable(){

			@Override
			public void run() {
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				String line = null;
				try {
					while ((line = reader.readLine()) != null) {
						line = line + "\n";
						if (TPKSystem.this.log != null) {
							TPKSystem.this.log.info(line);
						}
						if (output == null) continue;
						output.write(line.getBytes());
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		printThread.start();
		return printThread;
	}

	private boolean packageTEP(String buildDir, String pkgName) throws TPKException {
		String packagePath = PathUtil.addPath(buildDir, this.getTEPPackageName(pkgName));
		this.println("Zip paf dans ta gueule: " + packagePath);
		ZipLibrary zip = new ZipLibrary();
		try {
			File tepFile;
			if (this.outStream != null) {
				zip.setOutputStream(this.outStream);
			}
			if (!zip.zipping(tepFile = new File(this.bOpt.getTEPDirPath()), packagePath, 493)) {
				throw new TPKException(String.format("Cannot create package(%s)", packagePath));
			}
			return true;
		}
		catch (FileNotFoundException e) {
			throw new TPKException(e);
		}
		catch (IOException e) {
			throw new TPKException(e);
		}
		catch (Throwable e) {
			throw new TPKException(e);
		}
	}

	private String getTEPPackageName(String pkgName) {
		return String.format("%s%s%s", pkgName, "-", this.getTEPHashCode(this.bOpt.getTEPDirPath())) + ".tep";
	}

	private String getTEPHashCode(String tepPath) {
		if (tepPath == null || tepPath.isEmpty() || !FileUtil.existsPath(tepPath)) {
			return null;
		}
		StringBuffer lastModified = new StringBuffer(FileUtil.getLastModified(tepPath));
		if (lastModified.length() < 10) {
			for (int i = 0; i < 10 - lastModified.length(); ++i) {
				lastModified.append(0);
			}
		}
		return lastModified.substring(0, 10);
	}

	public synchronized boolean packaging(String buildDir, String pkgName, String pkgVersion, String arch, List<String> excludeList) throws TPKException {
		this.print("Zipping... \n");
		if (Performance.doPerformance) {
			Performance.setStartTime("Packaging");
		}
		if (!this.removeExcludeFileList(buildDir, excludeList)) {
			return false;
		}
		return this.packaging(buildDir, pkgName, pkgVersion, arch);
	}

	private boolean removeExcludeFileList(String buildDir, List<String> excludeList) {
		if (buildDir == null || excludeList == null) {
			return true;
		}
		try {
			String baseDir = PathUtil.addPath(buildDir, ".tpk");
			if (!FileUtil.removeFileList(baseDir, excludeList)) {
				this.setErrMsg(String.format("Cannot remove exclude file list.(%s)", excludeList.toString()));
				return false;
			}
			return true;
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
	}

	private String getPackagePath(String buildDir, String pkgName, String pkgVersion, String arch) {
		if (pkgName == null || pkgVersion == null) {
			return null;
		}
		if (arch != null && arch.equals("armel")) {
			arch = "arm";
		}
		return PathUtil.addPath(buildDir, StringUtil.addWordWithSeperator("-", pkgName, pkgVersion, arch) + ".tpk");
	}

	private boolean isMultiApp(String buildDir) {
		String multiAppConfigFilePath = PathUtil.addPath(this.getMultiPackagingHomePath(buildDir), "manifest_multi.xml");
		if (multiAppConfigFilePath == null) {
			return false;
		}
		File multiAppConfigFile = new File(multiAppConfigFilePath);
		if (multiAppConfigFile.exists()) {
			return true;
		}
		return false;
	}

	private boolean installMultiApp(String buildDir, String artifactName) {
		if (!this.installMultiAppDirectory("bin", "bin", buildDir)) {
			this.log.warn("Cannot install \"bin\" directory in multi-application");
		}
		if (!this.installMultiAppDirectory("res", "res", buildDir)) {
			this.log.warn("Cannot install \"res\" directory in multi-application");
		}
		if (!this.installMultiAppDirectory("shared", "shared", buildDir)) {
			this.log.warn("Cannot install \"shared\" directory in multi-application");
		}
		if (!this.installMultiAppDirectory("lib", "lib", buildDir)) {
			this.log.warn("Cannot install \"lib\" directory in multi-application");
		}
		if (!this.removeUnnecessaryPOFile(buildDir)) {
			this.log.warn("Cannot remove po directory.");
		}
		if (!this.installMultiAppFile(buildDir, artifactName)) {
			this.log.error("Cannot install multi app file.");
			return false;
		}
		return true;
	}

	private boolean installMultiAppDirectory(String src, String dst, String buildDir) {
		String srcDir = PathUtil.addPath(this.getMultiPackagingHomePath(buildDir), ".packaging", "temp" + src);
		String dstDir = PathUtil.addPath(buildDir, ".tpk", dst);
		File srcFile = new File(srcDir);
		if (!srcFile.exists()) {
			this.log.error("Multi app directory do not exist. => " + srcDir);
			return false;
		}
		try {
			for (File cFile : srcFile.listFiles()) {
				if (cFile.isFile()) {
					if (Command.copy(cFile.getAbsolutePath(), PathUtil.addPath(dstDir, cFile.getName()), "-r")) continue;
					this.log.warn("Cannot copy from " + srcDir + " to " + dstDir);
					continue;
				}
				if (Command.copy(cFile.getAbsolutePath(), dstDir, "-rf")) continue;
				this.log.warn("Cannot copy from " + srcDir + " to " + dstDir);
			}
			if (!Command.rm(srcDir)) {
				this.log.error("Cannot remove " + srcDir);
				return false;
			}
		}
		catch (IOException e) {
			this.log.exception(e);
			return false;
		}
		catch (InterruptedException e) {
			this.log.exception(e);
			return false;
		}
		return true;
	}

	private boolean installMultiAppFile(String buildDir, String artifactName) {
		if (this.isCoreProject(artifactName) ? !this.installCoreMultiConfiguration(buildDir) : !this.installNativeMultiConfiguration(buildDir)) {
			return false;
		}
		return true;
	}

	private boolean installNativeMultiConfiguration(String buildDir) {
		String tpkInfoDir = PathUtil.addPath(buildDir, ".tpk", "info");
		try {
			if (!Command.mkdir(tpkInfoDir)) {
				this.setErrMsg(String.format("Cannot create directory(%s)", tpkInfoDir));
				return false;
			}
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
		String srcFile = PathUtil.addPath(buildDir, "..", "manifest_multi.xml");
		String dstFile = PathUtil.addPath(tpkInfoDir, "manifest.xml");
		try {
			if (!Command.mv(srcFile, dstFile)) {
				this.setErrMsg(String.format("Cannot move from \"%s\" to \"%s\"", srcFile, dstFile));
				return false;
			}
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
		return true;
	}

	private boolean installCoreMultiConfiguration(String buildDir) {
		String srcFile = PathUtil.addPath(this.getMultiPackagingHomePath(buildDir), "manifest_multi.xml");
		String dstFile = PathUtil.addPath(buildDir, ".tpk", "tizen-manifest.xml");
		try {
			if (!Command.mv(srcFile, dstFile)) {
				this.setErrMsg(String.format("Cannot move from \"%s\" to \"%s\"", srcFile, dstFile));
				return false;
			}
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
		return true;
	}

	private String getMultiPackagingHomePath(String buildDir) {
		return buildDir;
	}

	private boolean installConfiguration(String buildDir, String artifactName) {
		if (this.isCoreProject(artifactName) ? !this.installCoreConfiguration(buildDir) : !this.installNativeConfiguration(buildDir)) {
			return false;
		}
		return true;
	}

	private boolean installNativeConfiguration(String buildDir) {
		String tpkInfoDir = PathUtil.addPath(buildDir, ".tpk", "info");
		try {
			if (!Command.mkdir(tpkInfoDir)) {
				this.setErrMsg(String.format("Cannot create directory(%s)", tpkInfoDir));
				return false;
			}
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
		String srcFile = PathUtil.addPath(buildDir, "..", "manifest.xml");
		String dstFile = PathUtil.addPath(tpkInfoDir, "manifest.xml");
		try {
			if (!Command.hardLinkCopy(srcFile, dstFile)) {
				this.setErrMsg(String.format("Cannot copy from \"%s\" to \"%s\"", srcFile, dstFile));
				return false;
			}
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
		return true;
	}

	private boolean installCoreConfiguration(String buildDir) {
		String srcFile = PathUtil.addPath(buildDir, "..", "tizen-manifest.xml");
		String dstFile = PathUtil.addPath(buildDir, ".tpk", "tizen-manifest.xml");
		if (PathUtil.existsFile(dstFile)) {
			try {
				Command.rm(dstFile);
			}
			catch (IOException e) {
				this.setErrMsg(e);
				return false;
			}
			catch (InterruptedException e) {
				this.setErrMsg(e);
				return false;
			}
		}
		try {
			if (!Command.hardLinkCopy(srcFile, dstFile)) {
				this.setErrMsg(String.format("Cannot copy form \"%s\" to \"%s\"", srcFile, dstFile));
				return false;
			}
		}
		catch (IOException e) {
			this.setErrMsg(e);
			return false;
		}
		catch (InterruptedException e) {
			this.setErrMsg(e);
			return false;
		}
		return true;
	}

	private boolean isCoreProject(String artifactName) {
		String extension = PathUtil.getExtension(artifactName);
		if (extension == null) {
			return true;
		}
		if (extension.equalsIgnoreCase("exe")) {
			return false;
		}
		return true;
	}

	private boolean isSign(ISigner signer) {
		return signer != null;
	}

	public void setOutputStream(OutputStream stream) {
		this.outStream = stream;
	}

	public void setErrorStream(OutputStream stream) {
		this.errStream = stream;
	}

	public void print(String message) {
		try {
			Printer.print(this.outStream, message);
		}
		catch (IOException e) {
			this.log.exception(e);
		}
	}

	public void println(String message) {
		if (message == null) {
			return;
		}
		this.print(message + "\n");
	}

	private void setErrMsg(String errMsg) {
		ErrorMessageController.setErrorMsg(errMsg);
		this.log.error(errMsg);
	}

	private void setErrMsg(Throwable e) {
		ErrorMessageController.setException(e);
		this.log.error(e.toString());
		this.log.exception(e);
	}

}

