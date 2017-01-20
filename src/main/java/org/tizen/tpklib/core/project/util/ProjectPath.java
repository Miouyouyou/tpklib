/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.core.project.util;

import java.io.IOException;
import org.tizen.tpklib.core.project.Project;
import org.tizen.tpklib.exception.TPKException;
import org.tizen.tpklib.lib.PathUtil;

public class ProjectPath {
    public static String getResourcePath(Project project) throws TPKException {
        if (project == null) {
            throw new TPKException("Illegal argument");
        }
        return PathUtil.addPath(project.getLocation(), "res");
    }

    public static String getTEPPath(Project project) throws TPKException {
        String resPath = ProjectPath.getResourcePath(project);
        return PathUtil.addPath(resPath, "tep");
    }

    public static String getTEPPath(String buildDir) throws TPKException {
        if (buildDir == null) {
            throw new TPKException("Illegal argument");
        }
        try {
            return PathUtil.addPath(PathUtil.getParentDirectory(buildDir), "res", "tep");
        }
        catch (IOException e) {
            throw new TPKException(e);
        }
    }
}

