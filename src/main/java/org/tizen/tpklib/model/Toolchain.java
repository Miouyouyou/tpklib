/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.model;

import java.io.File;
import java.util.List;
import java.util.Properties;
import org.tizen.tpklib.model.Plugin;
import org.tizen.tpklib.model.Tool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Toolchain
extends Plugin {
    public Toolchain(Document dom, File file) {
        super(dom, file);
    }

    @Override
    public String getId() {
        return this.getData("toolchain", "id", 0);
    }

    @Override
    public String getName() {
        return this.getData("toolchain", "name", 0);
    }

    public String getVersion() {
        return this.getData("toolchain", "version", 0);
    }

    public String getArchitecture() {
        return this.getData("toolchain", "architecture", 0);
    }

    public String getToolchainPath() {
        return this.getData("toolchain", "path", 0);
    }

    public String getToolchainType() {
        return this.getData("toolchain", "toolchainType", 0);
    }

    public String getProjectType() {
        Properties props = this.getProperties();
        if (props == null || props.isEmpty()) {
            return null;
        }
        String value = props.getProperty("PROJECT_TYPE");
        return value;
    }

    public boolean isDefault() {
        String defaultValue = this.getPropertyValue("DEFAULT");
        return Boolean.parseBoolean(defaultValue);
    }

    public Tool getCCompiler() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("c_compiler")) continue;
            return t;
        }
        return null;
    }

    public Tool getCppCompiler() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("c_compiler")) continue;
            return t;
        }
        return null;
    }

    public Tool getArchiver() {
        return this.getTool("archiver");
    }

    public Tool getDebugger() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("debugger")) continue;
            return t;
        }
        return null;
    }

    public Tool getAssembler() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("assembler")) continue;
            return t;
        }
        return null;
    }

    public Tool getStrip() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("strip")) continue;
            return t;
        }
        return null;
    }

    public Tool getEdjeCC() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("edje_cc")) continue;
            return t;
        }
        return null;
    }

    public Tool getMake() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("make")) continue;
            return t;
        }
        return null;
    }

    public Tool getMsgfmt() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("msgfmt")) continue;
            return t;
        }
        return null;
    }

    public Tool getTpkPackager() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("tpk_packager")) continue;
            return t;
        }
        return null;
    }

    public Tool getNativeSigner() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("native_signer")) continue;
            return t;
        }
        return null;
    }

    public Tool getBinutils() {
        List<Tool> toolList = this.getTools();
        if (toolList == null || toolList.isEmpty()) {
            return null;
        }
        for (Tool t : toolList) {
            if (!t.getName().equalsIgnoreCase("binutils")) continue;
            return t;
        }
        return null;
    }

    public List<Tool> getToolList() {
        return this.getTools();
    }

    public Element getMakeAction() {
        return this.getActionByName("make");
    }

    public Element getBuildPackageAction() {
        return this.getActionByName("buildpackage");
    }

    public Element getDebugAction() {
        return this.getActionByName("debug");
    }

    private Element getActionByName(String actionName) {
        if (actionName == null || actionName.isEmpty()) {
            return null;
        }
        NodeList actionList = this.getElementsByTagName("action");
        if (actionList == null) {
            return null;
        }
        for (int i = 0; i < actionList.getLength(); ++i) {
            Element e = (Element)actionList.item(i);
            String nameValue = e.getAttribute("name");
            if (!actionName.equalsIgnoreCase(nameValue)) continue;
            return e;
        }
        return null;
    }
}

