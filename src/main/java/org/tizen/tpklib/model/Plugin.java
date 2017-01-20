/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.model;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.tizen.tpklib.constants.SDKConstants;
import org.tizen.tpklib.constants.TPKConstants;
import org.tizen.tpklib.model.Property;
import org.tizen.tpklib.model.Tool;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class Plugin {
    protected Document dom = null;
    protected File file = null;
    protected boolean isChanged = false;

    public abstract String getName();

    public abstract String getId();

    public Plugin(Document dom, File file) {
        this.dom = dom;
        this.file = file;
    }

    public Document getDocument() {
        return this.dom;
    }

    public File getFile() {
        return this.file;
    }

    public String getLinkerMiscellaneousOption() {
        Properties props = this.getProperties();
        if (props == null || props.isEmpty()) {
            return "";
        }
        String value = props.getProperty("LINKER_MISCELLANEOUS_OPTION");
        if (value == null) {
            return "";
        }
        return value;
    }

    public String getTargetOption() {
        Properties props = this.getProperties();
        if (props == null || props.isEmpty()) {
            return "";
        }
        String value = props.getProperty("TARGET_OPTION");
        if (value == null) {
            return "";
        }
        return value;
    }

    public String getCompilerMiscellaneousOption() {
        Properties props = this.getProperties();
        if (props == null || props.isEmpty()) {
            return "";
        }
        String value = props.getProperty("COMPILER_MISCELLANEOUS_OPTION");
        if (value == null) {
            return "";
        }
        return value;
    }

    protected String getData(String tagName, String key, int order) {
        Element e = this.getElementByTagName(tagName, order);
        if (e == null) {
            return null;
        }
        return this.convertEnvironmentVariableToValue(e.getAttribute(key));
    }

    protected synchronized NodeList getElementsByTagName(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return null;
        }
        return this.dom.getElementsByTagName(tagName);
    }

    protected synchronized Element getElementByTagName(String tagName, int order) {
        if (tagName == null || tagName.isEmpty()) {
            return null;
        }
        NodeList nList = this.getElementsByTagName(tagName);
        if (nList == null || nList.getLength() < order + 1) {
            return null;
        }
        return (Element)nList.item(order);
    }

    protected String getAttributeValue(Element e, String attributeName) {
        if (e == null || attributeName == null) {
            return "";
        }
        String value = e.getAttribute(attributeName);
        if (value == null) {
            return "";
        }
        return value;
    }

    public String getExtensionPoint() {
        NodeList nList = this.getElementsByTagName("extension");
        if (nList == null || nList.getLength() <= 0) {
            return null;
        }
        Element extensionNode = (Element)nList.item(0);
        return extensionNode.getAttribute("point");
    }

    protected Properties getProperties() {
        NodeList nList = this.getElementsByTagName("property");
        Properties props = new Properties();
        for (int i = 0; i < nList.getLength(); ++i) {
            Node n = nList.item(i);
            Property prop = this.getProperty(n);
            if (prop == null) continue;
            props.setProperty(prop.key, prop.value);
        }
        return props;
    }

    protected Property getProperty(Node n) {
        String key = this.convertEnvironmentVariableToValue(this.getAttributeValue((Element)n, "key"));
        if (key == null || key.isEmpty()) {
            return null;
        }
        String value = this.convertEnvironmentVariableToValue(this.getAttributeValue((Element)n, "value"));
        return new Property(key, value);
    }

    protected List<Tool> getTools() {
        NodeList nList = this.getElementsByTagName("tool");
        ArrayList<Tool> toolList = new ArrayList<Tool>();
        for (int i = 0; i < nList.getLength(); ++i) {
            Node n = nList.item(i);
            Tool t = this.getTool(n);
            if (t == null) continue;
            toolList.add(t);
        }
        return toolList;
    }

    protected Tool getTool(Node n) {
        String path;
        String name = this.convertEnvironmentVariableToValue(this.getAttributeValue((Element)n, "name"));
        if (name == null || name.isEmpty()) {
            return null;
        }
        String version = this.getAttributeValue((Element)n, "version");
        if (version == null) {
            version = "";
        }
        if ((path = this.convertEnvironmentVariableToValue(this.getAttributeValue((Element)n, "path"))) == null) {
            path = "";
        }
        return new Tool(name, version, path);
    }

    public Tool getTool(String toolName) {
        if (toolName == null || toolName.isEmpty()) {
            return null;
        }
        List<Tool> toolList = this.getTools();
        for (Tool t : toolList) {
            if (!toolName.equalsIgnoreCase(t.getName())) continue;
            return t;
        }
        return null;
    }

    public String getPropertyValue(String propertyKey) {
        if (propertyKey == null || propertyKey.isEmpty()) {
            return null;
        }
        Properties props = this.getProperties();
        if (props == null || props.isEmpty()) {
            return null;
        }
        return props.getProperty(propertyKey);
    }

    protected String convertEnvironmentVariableToValue(String path) {
        if (path == null || path.isEmpty()) {
            return path;
        }
        String retPath = path;
        if (path.contains("#{SBI_HOME}")) {
            retPath = path.replace("#{SBI_HOME}", TPKConstants.SBI_HOME_PATH);
        }
        if (path.contains("#{SDK_DATA_PATH}")) {
            retPath = path.replace("#{SDK_DATA_PATH}", SDKConstants.getSDKDataPath());
        }
        if (path.contains("#{SDK_INSTALLED_PATH}")) {
            retPath = path.replace("#{SDK_INSTALLED_PATH}", SDKConstants.getInstalledPath());
        }
        if (path.contains("#{HOME}")) {
            retPath = path.replace("#{HOME}", SDKConstants.SDK_HOME_PATH);
        }
        return retPath;
    }

    private NodeList getNodeList(String tagName) {
        if (tagName == null || tagName.isEmpty()) {
            return null;
        }
        return this.getElementsByTagName(tagName);
    }

    protected Element getElement(String tagName, String attributeName, String attributeValue) {
        String value;
        NodeList nList = this.getNodeList(tagName);
        if (nList == null) {
            return null;
        }
        Element e = null;
        for (int i = 0; i < nList.getLength() && ((value = this.getAttributeValue(e = (Element)nList.item(i), attributeName)) == null || value.isEmpty() || !value.equals(attributeValue)); ++i) {
        }
        return e;
    }

    protected void setElement(String tagName, String findAttrName, String findValue, String changeAttrName, String changeValue) {
        Element e = this.getElement(tagName, findAttrName, findValue);
        if (e != null) {
            e.setAttribute(changeAttrName, changeValue);
            this.isChanged = true;
        }
    }

    public boolean isChanged() {
        return this.isChanged;
    }

    public String toString() {
        return this.getId();
    }
}

