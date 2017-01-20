/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.tizen.tpklib.exception.RootstrapException;
import org.tizen.tpklib.model.Plugin;
import org.tizen.tpklib.model.Tool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Rootstrap
extends Plugin
implements Comparable<Object> {
    private static final String[] PROFILE_VERSION_SEPERATOR_LIST = new String[]{" ", "-"};
    public static final String PROPERTY_DEFAULT = "DEFAULT";
    private Tool defaultToolchain = null;

    public Rootstrap(Document dom, File file) {
        super(dom, file);
    }

    @Override
    public String getId() {
        return this.getData("rootstrap", "id", 0);
    }

    @Override
    public String getName() {
        return this.getData("rootstrap", "name", 0);
    }

    public String getPath() {
        return this.getData("rootstrap", "path", 0);
    }

    public String getVersion() {
        return this.getData("rootstrap", "version", 0);
    }

    public String getProfile() {
        String[] profileInfo = this.getProfileInfo();
        if (profileInfo == null || profileInfo.length < 2) {
            return "";
        }
        return profileInfo[0];
    }

    public String getProfileVersion() {
        String[] profileInfo = this.getProfileInfo();
        if (profileInfo == null || profileInfo.length < 2) {
            return "";
        }
        return profileInfo[1];
    }

    private String[] getProfileInfo(String profileVersion) {
        for (String seperator : PROFILE_VERSION_SEPERATOR_LIST) {
            if (!profileVersion.contains(seperator)) continue;
            return profileVersion.split(seperator);
        }
        return null;
    }

    private String[] getProfileInfo() {
        return this.getProfileInfo(this.getVersion());
    }

    public String getArchitecture() {
        return this.getData("rootstrap", "architecture", 0);
    }

    public String getSupportToolchainType() {
        return this.getData("rootstrap", "supportToolchainType", 0);
    }

    public String getToolchainType() {
        return this.getData("rootstrap", "toolchainType", 0);
    }

    public Tool getDefaultToolchain() throws RootstrapException {
        if (this.defaultToolchain != null) {
            return this.defaultToolchain;
        }
        Element e = this.getElementByTagName("toolchain", 0);
        if (e == null) {
            throw new RootstrapException(String.format("Cannot find the element (%s)", "toolchain"));
        }
        String name = this.convertEnvironmentVariableToValue(e.getAttribute("name")).toUpperCase();
        if (name == null) {
            throw new RootstrapException(String.format("Cannot find the name of %s", "toolchain"));
        }
        String version = this.convertEnvironmentVariableToValue(e.getAttribute("version"));
        return new Tool(name, version, null);
    }

    public List<Tool> getDefaultToolchainList() throws RootstrapException {
        NodeList nList = this.getElementsByTagName("toolchain");
        if (nList == null || nList.getLength() <= 0) {
            throw new RootstrapException(String.format("Cannot find the element (%s)", "toolchain"));
        }
        ArrayList<Tool> defaultToolchainList = new ArrayList<Tool>();
        for (int i = 0; i < nList.getLength(); ++i) {
            Element e = (Element)nList.item(i);
            String name = this.convertEnvironmentVariableToValue(e.getAttribute("name")).toUpperCase();
            if (name == null) {
                throw new RootstrapException(String.format("Cannot find the name of %s", "toolchain"));
            }
            String version = this.convertEnvironmentVariableToValue(e.getAttribute("version"));
            if (version == null || version.isEmpty()) {
                throw new RootstrapException(String.format("Cannot find the version of %s", "toolchain"));
            }
            defaultToolchainList.add(new Tool(name, version, null));
        }
        return defaultToolchainList;
    }

    public boolean isSupportToolchainType(String toolchainType) {
        if (toolchainType == null || toolchainType.isEmpty()) {
            return false;
        }
        String thisSupportToolchainType = this.getSupportToolchainType();
        if (thisSupportToolchainType.equals("(null)") || thisSupportToolchainType.equals("all") || thisSupportToolchainType.equals(toolchainType)) {
            return true;
        }
        return false;
    }

    public boolean isDefault() {
        Properties props = this.getProperties();
        String value = props.getProperty("DEFAULT");
        if (value == null) {
            return false;
        }
        if (value.equalsIgnoreCase(Boolean.TRUE.toString())) {
            return true;
        }
        return false;
    }

    public void setDefault(boolean isDefault) {
        this.setElement("property", "key", "DEFAULT", "value", Boolean.toString(isDefault));
    }

    public String getProperty(String key) {
        Properties props = this.getProperties();
        if (props == null || props.isEmpty()) {
            return null;
        }
        return props.getProperty(key);
    }

    @Override
    public int compareTo(Object o) {
        if (o == null) {
            throw new NullPointerException();
        }
        String dstVersion = null;
        if (o instanceof Rootstrap) {
            dstVersion = ((Rootstrap)o).getProfileVersion();
        } else if (o instanceof String) {
            String[] profileVersion = this.getProfileInfo((String)o);
            dstVersion = profileVersion == null ? (String)o : profileVersion[1];
        } else {
            throw new ClassCastException();
        }
        return this.getProfileVersion().compareTo(dstVersion);
    }
}

