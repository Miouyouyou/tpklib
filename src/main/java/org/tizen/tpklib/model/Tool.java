/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.model;

import org.tizen.tpklib.lib.PathUtil;

public class Tool {
    public String name;
    public String version;
    public String path;

    public Tool(String name, String version, String path) {
        this.name = name;
        this.version = version;
        this.path = path;
    }

    public String getName() {
        return this.name;
    }

    public String getVersion() {
        return this.version;
    }

    public String getPath() {
        return this.path;
    }

    public String getToolName() {
        return PathUtil.getFileName(this.path);
    }
}

