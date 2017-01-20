/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.model;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.tizen.tpklib.TPK;
import org.tizen.tpklib.constants.SBIConstants;
import org.tizen.tpklib.constants.SDKConstants;
import org.tizen.tpklib.exception.RootstrapException;
import org.tizen.tpklib.exception.RootstrapNotFoundException;
import org.tizen.tpklib.exception.RootstrapParsingException;
import org.tizen.tpklib.exception.RootstrapUnsupportedTypeException;
import org.tizen.tpklib.exception.ToolchainNotFoundException;
import org.tizen.tpklib.lib.ArgumentValidation;
import org.tizen.tpklib.lib.Log;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.XmlParser;
import org.tizen.tpklib.model.Plugin;
import org.tizen.tpklib.model.Rootstrap;
import org.tizen.tpklib.model.Target;
import org.tizen.tpklib.model.Tool;
import org.tizen.tpklib.model.Toolchain;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PluginManager {
    private static PluginManager pm = null;
    List<Rootstrap> rootstrapList = new CopyOnWriteArrayList<Rootstrap>();
    List<Toolchain> toolchainList = new CopyOnWriteArrayList<Toolchain>();
    List<Target> targetList = new CopyOnWriteArrayList<Target>();
    public static final String[] ARCHGROUP_X86 = new String[]{"86", "X86", "i386", "ia32", "i586", "i686", "LLVM-Bitcode/X86"};
    public static final String[] ARCHGROUP_ARM = new String[]{"arm", "ARMv7-a", "armel", "armv7l", "LLVM-Bitcode/ARM"};
    private Log log = TPK.log;

    private PluginManager() {
        this.init();
    }

    private void init() {
        if (this.log == null) {
            this.log = TPK.log = TPK.createLog();
        }
        this.clear();
        List<File> pluginFileList = this.getPluginFileList();
        this.loadPlugin(pluginFileList);
    }

    private void loadPlugin(List<File> pluginFileList) {
        this.log.info("Load plugin from file => " + pluginFileList + "\n");
        for (File pluginFile : pluginFileList) {
            if (!"xml".equalsIgnoreCase(PathUtil.getExtension(pluginFile.getName()))) continue;
            Document dom = null;
            try {
                dom = XmlParser.parsing(pluginFile);
            }
            catch (ParserConfigurationException e) {
                this.log.exception(e);
                return;
            }
            catch (SAXException e) {
                this.log.exception(e);
                return;
            }
            catch (IOException e) {
                this.log.exception(e);
                return;
            }
            if (dom == null) {
                this.log.error("Cannot load plugin");
                return;
            }
            Element extensionNode = (Element)dom.getElementsByTagName("extension").item(0);
            String value = extensionNode.getAttribute("point");
            if (value.equalsIgnoreCase("rootstrapDefinition")) {
                this.rootstrapList.add(new Rootstrap(dom, pluginFile));
                continue;
            }
            if (value.equalsIgnoreCase("toolchainDefinition")) {
                this.toolchainList.add(new Toolchain(dom, pluginFile));
                continue;
            }
            if (value.equalsIgnoreCase("targetDefinition")) {
                this.targetList.add(new Target(dom, pluginFile));
                continue;
            }
            this.log.warn("Unsupported plugin type. => " + value);
        }
    }

    public Rootstrap loadRootstrap(String rsPath) throws RootstrapException {
        if (rsPath == null || rsPath.isEmpty()) {
            throw new RootstrapNotFoundException(String.format("Cannot find rootstrap: %s", rsPath));
        }
        File rsFile = new File(rsPath);
        Document dom = null;
        try {
            dom = XmlParser.parsing(rsFile);
        }
        catch (IOException | ParserConfigurationException | SAXException e) {
            throw new RootstrapParsingException(e);
        }
        if (dom == null) {
            throw new RootstrapParsingException("Cannot load rootstrap: " + rsPath);
        }
        Element extensionNode = (Element)dom.getElementsByTagName("extension").item(0);
        String value = extensionNode.getAttribute("point");
        if (!"rootstrapDefinition".equalsIgnoreCase(value)) {
            throw new RootstrapUnsupportedTypeException(String.format("Unsupported type: %s", value));
        }
        return new Rootstrap(dom, rsFile);
    }

    public void loadPlugin(String pluginId) {
        String pluginDirPath = this.getPluginPath();
        String pluginFileName = pluginId + "." + "xml";
        File pluginFile = new File(PathUtil.addPath(pluginDirPath, pluginFileName));
        this.loadPlugin(pluginFile);
    }

    private void loadPlugin(File pFile) {
        if (!pFile.exists() || !"xml".equalsIgnoreCase(PathUtil.getExtension(pFile.getName()))) {
            return;
        }
        Document dom = null;
        try {
            dom = XmlParser.parsing(pFile);
        }
        catch (ParserConfigurationException e) {
            this.log.exception(e);
            return;
        }
        catch (SAXException e) {
            this.log.exception(e);
            return;
        }
        catch (IOException e) {
            this.log.exception(e);
            return;
        }
        if (dom == null) {
            this.log.error("Cannot load plugin");
            return;
        }
        Element extensionNode = (Element)dom.getElementsByTagName("extension").item(0);
        String value = extensionNode.getAttribute("point");
        if (value.equalsIgnoreCase("rootstrapDefinition")) {
            this.addRootstrap(new Rootstrap(dom, pFile));
        } else if (value.equalsIgnoreCase("toolchainDefinition")) {
            this.addToolchain(new Toolchain(dom, pFile));
        } else if (value.equalsIgnoreCase("targetDefinition")) {
            this.addTarget(new Target(dom, pFile));
        } else {
            this.log.warn("Unsupported plugin type. => " + value);
        }
    }

    public void reInit() {
        this.init();
    }

    private void clear() {
        if (this.rootstrapList.size() > 0) {
            this.rootstrapList.clear();
        }
        if (this.toolchainList.size() > 0) {
            this.toolchainList.clear();
        }
        if (this.targetList.size() > 0) {
            this.targetList.clear();
        }
    }

    private String getPluginPath() {
        return PathUtil.addPath(SBIConstants.getSBIPluginPath());
    }

    public synchronized void addRootstrap(Rootstrap rootstrap) {
        if (rootstrap == null) {
            this.log.info("Cannot add rootstrap => " + rootstrap);
            return;
        }
        Rootstrap tmpRootstrap = null;
        for (Rootstrap r : this.rootstrapList) {
            if (!r.getId().equals(rootstrap.getId())) continue;
            tmpRootstrap = r;
            break;
        }
        if (tmpRootstrap != null) {
            this.rootstrapList.remove(tmpRootstrap);
        }
        this.rootstrapList.add(rootstrap);
    }

    public synchronized boolean removeRootstrap(Rootstrap rootstrap) {
        if (rootstrap == null) {
            this.log.warn("Rootstrap is null");
            return false;
        }
        if (!rootstrap.getFile().delete()) {
            this.log.error("Cannot remove the rootstrap file => " + rootstrap.getFile().getAbsolutePath());
            return false;
        }
        if (!this.rootstrapList.remove(rootstrap)) {
            this.log.warn("Target do not exist in rootstrap-list => " + rootstrap);
            this.log.warn("Rootstrap-list => " + this.rootstrapList);
            return false;
        }
        return true;
    }

    public synchronized void addToolchain(Toolchain toolchain) {
        if (toolchain == null) {
            this.log.info("Cannot add toolchain => " + toolchain);
            return;
        }
        Toolchain tmpToolchain = null;
        for (Toolchain tc : this.toolchainList) {
            if (!tc.getId().equals(toolchain.getId())) continue;
            tmpToolchain = tc;
            break;
        }
        if (tmpToolchain != null) {
            this.toolchainList.remove(tmpToolchain);
        }
        this.toolchainList.add(toolchain);
    }

    public synchronized void addTarget(Target target) {
        if (target == null) {
            this.log.info("Cannot add target => " + target);
            return;
        }
        Target tmpTarget = null;
        for (Target tg : this.targetList) {
            if (!tg.getId().equals(target.getId())) continue;
            tmpTarget = tg;
            break;
        }
        if (tmpTarget != null) {
            this.targetList.remove(tmpTarget);
        }
        this.targetList.add(target);
    }

    public boolean createTarget(String rootstrapId, String toolchainId) {
        return this.createTarget(rootstrapId, toolchainId, null);
    }

    public boolean createTarget(String rootstrapId, String toolchainId, String targetId) {
        return this.createTarget(rootstrapId, toolchainId, targetId, targetId);
    }

    public boolean createTarget(String rootstrapId, String toolchainId, String targetId, String targetName) {
        this.log.info("Create target from rootstrap and toolchain");
        this.log.info("target id : " + targetId);
        if (targetId == null || targetId.isEmpty()) {
            targetId = this.makeTargetId(rootstrapId, toolchainId);
        }
        if (!this.isAvailable(rootstrapId, toolchainId, targetId)) {
            this.log.error("Cannot create target");
            return false;
        }
        Document tDom = null;
        try {
            tDom = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        }
        catch (ParserConfigurationException e) {
            this.log.exception(e);
            return false;
        }
        Element extensionElement = tDom.createElement("extension");
        tDom.getDocumentElement();
        tDom.appendChild(extensionElement);
        extensionElement.setAttribute("point", "targetDefinition");
        Element targetElement = tDom.createElement("target");
        extensionElement.appendChild(targetElement);
        targetElement.setAttribute("id", targetId);
        targetElement.setAttribute("name", targetName);
        targetElement.setAttribute("rootstrapId", rootstrapId);
        targetElement.setAttribute("toolchainId", toolchainId);
        String targetFilePath = PathUtil.addPath(SBIConstants.getSBIPluginPath(), targetId + ".xml");
        File targetFile = new File(targetFilePath);
        if (!this.savePlugin(tDom, targetFile)) {
            return false;
        }
        this.targetList.add(new Target(tDom, targetFile));
        return true;
    }

    private synchronized boolean savePlugin(Document dom, File xmlFile) {
        this.log.info("save plugin to xml file => " + xmlFile);
        if (dom == null || xmlFile == null) {
            this.log.error("Cannot save plugin.");
            return false;
        }
        if (xmlFile.exists() && xmlFile.delete()) {
            this.log.error("Cannot delete plugin file => " + xmlFile);
        }
        DOMSource source = new DOMSource(dom);
        Transformer transformer = null;
        try {
            transformer = TransformerFactory.newInstance().newTransformer();
        }
        catch (TransformerConfigurationException e) {
            this.log.exception(e);
            return false;
        }
        catch (TransformerFactoryConfigurationError e) {
            this.log.exception(e);
            return false;
        }
        StreamResult result = new StreamResult(xmlFile);
        try {
            transformer.transform(source, result);
        }
        catch (TransformerException e) {
            this.log.exception(e);
            return false;
        }
        return true;
    }

    private boolean isAvailable(String rId, String cId, String tId) {
        if (!this.isAvailableRootstrapId(rId)) {
            this.log.error("Cannot find rootstrap id => " + rId);
            this.log.error("rootstrap list => " + this.rootstrapList);
            return false;
        }
        if (!this.isAvailableToolchainId(cId)) {
            this.log.error("Cannot find toolchain id => " + cId);
            this.log.error("toolchain list => " + this.toolchainList);
            return false;
        }
        if (!this.isTargetIdAvailableToCreate(tId)) {
            return false;
        }
        return true;
    }

    private boolean isTargetIdAvailableToCreate(String id) {
        if (this.isAvailableRootstrapId(id)) {
            this.log.error("ID is not available. => " + id);
            this.log.error("exist in rootstrap list => " + this.rootstrapList);
            return false;
        }
        if (this.isAvailableToolchainId(id)) {
            this.log.error("ID is not available. => " + id);
            this.log.error("exist in toolchain list => " + this.toolchainList);
            return false;
        }
        if (this.exist(this.targetList, id)) {
            this.log.error("ID is not available. => " + id);
            this.log.error("exist in target list => " + this.targetList);
            return false;
        }
        return true;
    }

    public synchronized boolean removeTarget(String tId) {
        if (tId == null || tId.isEmpty()) {
            this.log.warn("Target ID is empty => " + tId);
            return false;
        }
        Target t = this.getTarget(tId);
        return this.removeTarget(t);
    }

    public synchronized boolean removeTarget(Target target) {
        if (target == null) {
            this.log.warn("Target do not exist in target-list => " + target);
            this.log.warn("target-list => " + this.targetList);
            return false;
        }
        if (!target.getFile().delete()) {
            this.log.error("Cannot remove the target file => " + target.getFile().getAbsolutePath());
            return false;
        }
        if (!this.targetList.remove(target)) {
            this.log.warn("Target do not exist in target-list => " + target);
            this.log.warn("target-list => " + this.targetList);
            return false;
        }
        return true;
    }

    public String makeTargetId(String rId, String cId) {
        return rId + "_" + cId;
    }

    public Properties getPluginInfo(String pId) {
        Plugin p = this.getPlugin(pId);
        if (p == null) {
            return null;
        }
        String point = p.getExtensionPoint();
        if (point == null) {
            this.log.warn("Cannot find plugin => " + pId);
            return null;
        }
        if (point.equalsIgnoreCase("rootstrapDefinition")) {
            return this.getRootstrapInfo((Rootstrap)p);
        }
        if (point.equalsIgnoreCase("toolchainDefinition")) {
            return this.getToolchainInfo((Toolchain)p);
        }
        if (point.equalsIgnoreCase("targetDefinition")) {
            return this.getTargetInfo((Target)p);
        }
        this.log.error("Cannot find plugin id => " + pId);
        return null;
    }

    public Properties getRootstrapInfo(String rId) {
        Rootstrap r = this.getRootstrap(rId);
        return this.getRootstrapInfo(r);
    }

    public Properties getToolchainInfo(String cId) {
        Toolchain t = this.getToolchain(cId);
        return this.getToolchainInfo(t);
    }

    public Properties getTargetInfo(String tId) {
        Target t = this.getTarget(tId);
        return this.getTargetInfo(t);
    }

    private Properties getRootstrapInfo(Rootstrap rId) {
        return null;
    }

    private Properties getToolchainInfo(Toolchain cId) {
        return null;
    }

    private Properties getTargetInfo(Target tId) {
        return null;
    }

    public boolean savePlugin() {
        return false;
    }

    public boolean saveRootstrap() {
        return false;
    }

    public boolean saveToolchain() {
        return false;
    }

    public boolean saveTarget() {
        return false;
    }

    public List<Rootstrap> getRootstrapList() {
        return this.rootstrapList;
    }

    public List<Rootstrap> getRootstrapListFromProfile(String profile) {
        ArrayList<Rootstrap> rsList = new ArrayList<Rootstrap>();
        if (profile == null || profile.isEmpty()) {
            return rsList;
        }
        for (Rootstrap rs : this.rootstrapList) {
            if (!rs.getId().startsWith(profile.toLowerCase())) continue;
            rsList.add(rs);
        }
        return rsList;
    }

    public List<Toolchain> getToolchainList() {
        return this.toolchainList;
    }

    public List<Toolchain> getAvailableToolchainList(String rootstrapId, String projectType) {
        ArrayList<Toolchain> availableToolchainList = new ArrayList<Toolchain>();
        List<Toolchain> toolchainList = this.getAvailableToolchainList(rootstrapId);
        for (Toolchain tc : toolchainList) {
            String pType = tc.getProjectType();
            if (pType == null || !pType.equals(projectType)) continue;
            availableToolchainList.add(tc);
        }
        return availableToolchainList;
    }

    public List<Toolchain> getAvailableToolchainList(String rootstrapId) {
        ArrayList<Toolchain> availableToolchainList = new ArrayList<Toolchain>();
        Rootstrap rs = this.getRootstrap(rootstrapId);
        if (rs == null) {
            return availableToolchainList;
        }
        String arch = rs.getArchitecture();
        String toolchainType = rs.getSupportToolchainType();
        String[] candidateArchs = null;
        if (PluginManager.isX86Arch(arch)) {
            candidateArchs = ARCHGROUP_X86;
        } else if (PluginManager.isArmArch(arch)) {
            candidateArchs = ARCHGROUP_ARM;
        } else {
            return availableToolchainList;
        }
        for (Toolchain tc : this.toolchainList) {
            if (!PluginManager.containValue(candidateArchs, tc.getArchitecture()) || !toolchainType.equals(tc.getToolchainType())) continue;
            availableToolchainList.add(tc);
        }
        return availableToolchainList;
    }

    public List<String> getAvailableToolchainIdList(String rootstrapId, String projectType) {
        List<Toolchain> availableToolchainList = this.getAvailableToolchainList(rootstrapId, projectType);
        ArrayList<String> availableToolchainIdList = new ArrayList<String>();
        for (Toolchain tc : availableToolchainList) {
            availableToolchainIdList.add(tc.getId());
        }
        return availableToolchainIdList;
    }

    public List<Rootstrap> getAvailableRootstrapList(String supportedToolchainType) {
        ArrayList<Rootstrap> availableRootstrapIdList = new ArrayList<Rootstrap>();
        if (supportedToolchainType == null || supportedToolchainType.isEmpty()) {
            return availableRootstrapIdList;
        }
        for (Rootstrap rs : this.rootstrapList) {
            if (!rs.getSupportToolchainType().equals(supportedToolchainType)) continue;
            availableRootstrapIdList.add(rs);
        }
        return availableRootstrapIdList;
    }

    public boolean isX86Architecture(String arch) {
        return PluginManager.isX86Arch(arch);
    }

    public static boolean isX86Arch(String arch) {
        if (arch == null) {
            return false;
        }
        for (String a : ARCHGROUP_X86) {
            if (!a.equals(arch)) continue;
            return true;
        }
        return false;
    }

    public boolean isArmArchitecture(String arch) {
        return PluginManager.isArmArch(arch);
    }

    public static boolean isArmArch(String arch) {
        if (arch == null) {
            return false;
        }
        for (String a : ARCHGROUP_ARM) {
            if (!a.equals(arch)) continue;
            return true;
        }
        return false;
    }

    public static boolean containValue(String[] strs, String value) {
        for (String str : strs) {
            if (!str.equals(value)) continue;
            return true;
        }
        return false;
    }

    public List<Target> getTargetList() {
        return this.targetList;
    }

    public List<Plugin> getPluginList() {
        ArrayList<Plugin> pList = new ArrayList<Plugin>();
        pList.addAll(this.rootstrapList);
        pList.addAll(this.toolchainList);
        pList.addAll(this.targetList);
        return pList;
    }

    public Rootstrap getRootstrap(String rId) {
        return (Rootstrap)this.getPluginById(this.rootstrapList, rId);
    }

    public Toolchain getDefaultToolchain(String rId, String projectType) throws ToolchainNotFoundException, RootstrapException {
        return this.getDefaultToolchain(this.getRootstrap(rId), projectType);
    }

    public Toolchain getDefaultToolchain(Rootstrap rs, String projectType) throws ToolchainNotFoundException, RootstrapException {
        Tool tool = rs.getDefaultToolchain();
        return this.getToolchain(tool.getName(), tool.getVersion(), rs.getArchitecture(), projectType);
    }

    public Toolchain getToolchain(String profile, String version, String arch, String projectType) throws ToolchainNotFoundException {
        String toolchainName = PluginManager.getPlatform(profile, version);
        for (Toolchain tc : this.toolchainList) {
            if (!tc.getName().equalsIgnoreCase(toolchainName) || !tc.getArchitecture().equals(arch) || !tc.getProjectType().equals(projectType)) continue;
            return tc;
        }
        throw new ToolchainNotFoundException();
    }

    public Toolchain getToolchain(String cId) {
        return (Toolchain)this.getPluginById(this.toolchainList, cId);
    }

    public Target getTarget(String tId) {
        return (Target)this.getPluginById(this.targetList, tId);
    }

    public Target getTargetByName(String name) {
        for (Target t : this.targetList) {
            if (!t.getName().equals(name)) continue;
            return t;
        }
        return null;
    }

    public Plugin getPlugin(String pId) {
        return this.getPluginById(pId);
    }

    public Toolchain getToolchainByTargetId(String targetId) {
        if (targetId == null || targetId.isEmpty()) {
            return null;
        }
        Target t = this.getTarget(targetId);
        String toolchainId = null;
        toolchainId = t == null ? this.getToolchainIDFromTargetID(targetId) : t.getToolchainId();
        return this.getToolchain(toolchainId);
    }

    public Rootstrap getRootstrapByTargetId(String targetId) {
        if (targetId == null || targetId.isEmpty()) {
            return null;
        }
        Target t = this.getTarget(targetId);
        String rootstrapId = null;
        rootstrapId = t == null ? this.getRootstrapIDFromTargetID(targetId) : t.getRootstrapId();
        return this.getRootstrap(rootstrapId);
    }

    public Tool getToolByTargetId(String targetId, String toolName) {
        if (targetId == null || targetId.isEmpty()) {
            this.log.warn("target id is empty.");
            return null;
        }
        if (toolName == null || toolName.isEmpty()) {
            this.log.warn("tool name is empty.");
            return null;
        }
        Toolchain tc = this.getToolchainByTargetId(targetId);
        if (tc == null) {
            this.log.warn("Cannot find toolchain from target id => " + targetId);
            return null;
        }
        Tool tool = tc.getTool(toolName);
        return tool;
    }

    public String getRootstrapIDFromTargetID(String targetId) {
        int index = targetId.lastIndexOf("_");
        if (index < 0) {
            return "";
        }
        return targetId.substring(0, index);
    }

    public String getToolchainIDFromTargetID(String targetId) {
        int index = targetId.lastIndexOf("_");
        if (index < 0) {
            return "";
        }
        return targetId.substring(index + 1, targetId.length());
    }

    private Plugin getPluginById(String pId) {
        if (pId == null || pId.isEmpty()) {
            return null;
        }
        return this.getPluginById(this.getPluginList(), pId);
    }

    private synchronized Plugin getPluginById(List<? extends Plugin> pList, String pId) {
        if (pList == null) {
            return this.getPluginById(pId);
        }
        if (pId == null || pId.isEmpty()) {
            return null;
        }
        for (Plugin p : pList) {
            String id = p.getId();
            if (id == null || !p.getId().equals(pId)) continue;
            return p;
        }
        this.log.warn("Cannot find plugin => " + pId);
        return null;
    }

    public boolean isAvailableTargetId(String targetId) {
        if (targetId == null || targetId.isEmpty()) {
            return false;
        }
        String toolchainId = this.getToolchainIDFromTargetID(targetId);
        if (!this.isAvailableToolchainId(toolchainId)) {
            this.log.warn(toolchainId + " do not exist");
            return false;
        }
        String rootstrapId = this.getRootstrapIDFromTargetID(targetId);
        if (!this.isAvailableRootstrapId(rootstrapId)) {
            this.log.warn(rootstrapId + " do not exist");
            return false;
        }
        if (!this.exist(this.targetList, targetId)) {
            this.log.warn(targetId + " do not exist");
            this.log.warn("Target list => " + this.targetList);
            return false;
        }
        return true;
    }

    public boolean isAvailableRootstrapId(String rootstrapId) {
        if (!this.exist(this.rootstrapList, rootstrapId)) {
            this.log.warn(rootstrapId + " do not exist");
            this.log.warn("Target list => " + this.rootstrapList);
            return false;
        }
        return true;
    }

    public boolean isAvailableToolchainId(String toolchainId) {
        if (!this.exist(this.toolchainList, toolchainId)) {
            this.log.warn(toolchainId + " do not exist");
            this.log.warn("Target list => " + this.toolchainList);
            return false;
        }
        return true;
    }

    private boolean exist(List<? extends Plugin> pList, String pId) {
        Plugin p = this.getPluginById(pList, pId);
        if (p != null) {
            return true;
        }
        return false;
    }

    public boolean isEmpty() {
        if (this.rootstrapList.isEmpty() && this.toolchainList.isEmpty() && this.targetList.isEmpty()) {
            return true;
        }
        return false;
    }

    private List<File> getPluginFileList() {
        String pluginPath = this.getPluginPath();
        this.log.info(String.format("Get plugin from \"%s\"", pluginPath));
        File pluginDirFile = new File(pluginPath);
        if (!pluginDirFile.exists()) {
            return Collections.emptyList();
        }
        return Arrays.asList(pluginDirFile.listFiles());
    }

    public static synchronized PluginManager getInstance() {
        if (pm == null) {
            pm = new PluginManager();
        }
        return pm;
    }

    public static synchronized PluginManager getInstance(String sdkInstalledPath) {
        SDKConstants.setUserInstalledPath(sdkInstalledPath);
        if (pm == null) {
            pm = new PluginManager();
        }
        return pm;
    }

    public static String getPlatform(String profile, String version) {
        if (!ArgumentValidation.validateStringArgument(profile, version)) {
            return null;
        }
        return profile + "-" + version;
    }

    public static String getVersionFromPlatform(String platformVersion) {
        String[] platformInfos = PluginManager.getPlatformInfo(platformVersion);
        if (platformInfos == null || platformInfos.length < 2) {
            return null;
        }
        return platformInfos[1];
    }

    public static String getProfileFromPlatform(String platformVersion) {
        String[] platformInfos = PluginManager.getPlatformInfo(platformVersion);
        if (platformInfos == null || platformInfos.length < 1) {
            return null;
        }
        return platformInfos[0];
    }

    public static String[] getPlatformInfo(String platformVersion) {
        if (platformVersion == null) {
            return null;
        }
        return platformVersion.split("-");
    }
}

