/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.model;

import java.io.File;
import org.tizen.tpklib.model.Plugin;
import org.w3c.dom.Document;

public class Target
extends Plugin {
    public Target(Document dom, File file) {
        super(dom, file);
    }

    @Override
    public String getId() {
        return this.getData("target", "id", 0);
    }

    @Override
    public String getName() {
        return this.getData("target", "name", 0);
    }

    public String getRootstrapId() {
        return this.getData("target", "rootstrapId", 0);
    }

    public String getToolchainId() {
        return this.getData("target", "toolchainId", 0);
    }
}

