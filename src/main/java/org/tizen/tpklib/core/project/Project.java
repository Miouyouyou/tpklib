/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.core.project;

import org.tizen.tpklib.exception.TPKException;

public class Project {
    private String location = null;
    private ProjectType pType = ProjectType.APP;
    private Configuration config = Configuration.DEBUG;

    public Project(String location) throws TPKException {
        this(location, null, null);
    }

    public Project(String location, String configuration, String type) throws TPKException {
        Configuration bMode;
        ProjectType pType;
        if (location == null) {
            throw new TPKException("Cannot find project");
        }
        this.location = location;
        if (configuration != null && (bMode = Configuration.valueOf(configuration.toUpperCase())) != null) {
            this.config = bMode;
        }
        if (type != null && (pType = ProjectType.valueOf(type.toUpperCase())) != null) {
            this.pType = pType;
        }
    }

    public String getLocation() {
        return this.location;
    }

    public Configuration getConfiguration() {
        return this.config;
    }

    public ProjectType getProjectType() {
        return this.pType;
    }

    public static enum ProjectType {
        APP("app", "bin"),
        IME("ime", "lib");
        
        private String pType = null;
        private String dir = null;

        private ProjectType(String type, String dir) {
            this.pType = type;
            this.dir = dir;
        }

        public String getDir() {
            return this.dir;
        }

        public String toString() {
            return this.pType;
        }
    }

    public static enum Configuration {
        DEBUG("debug", "Debug"),
        RELEASE("release", "Release");
        
        private String mode = null;
        private String dir = null;

        private Configuration(String mode, String dir) {
            this.mode = mode;
            this.dir = dir;
        }

        public String toString() {
            return this.mode;
        }

        public String getDir() {
            return this.dir;
        }
    }

}

