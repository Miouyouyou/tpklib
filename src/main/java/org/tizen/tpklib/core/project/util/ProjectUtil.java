/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.core.project.util;

import java.io.File;
import org.tizen.tpklib.core.project.Project;
import org.tizen.tpklib.core.project.ProjectConstants;
import org.tizen.tpklib.exception.TPKException;
import org.tizen.tpklib.lib.ArgumentValidation;

public class ProjectUtil {
    public static String getProjectLocation(String buildDir) throws TPKException {
        if (!ArgumentValidation.validateStringArgument(buildDir)) {
            throw new TPKException("Illegal argument");
        }
        return new File(buildDir).getParent();
    }

    public static String getConfigurationName(String buildDir) throws TPKException {
        if (!ArgumentValidation.validateStringArgument(buildDir)) {
            throw new TPKException("Illegal argument");
        }
        return new File(buildDir).getName();
    }

    public static String getBuildDirName(Project project) throws TPKException {
        if (project == null) {
            throw new TPKException("Illeagal argument");
        }
        return project.getConfiguration().getDir();
    }

    public static String getProjectTypeName(String artifact) throws TPKException {
        if (artifact == null) {
            throw new TPKException("Illeagal argument");
        }
        if (artifact.equals(ProjectConstants.SHARED_LIBRARY_BINARY_EXTENSION) || artifact.equals(ProjectConstants.STATIC_LIBRARY_BINARY_EXTENSION)) {
            return Project.ProjectType.IME.toString();
        }
        return Project.ProjectType.APP.toString();
    }

    public static String getBinDirName(Project project) throws TPKException {
        if (project == null) {
            throw new TPKException("Illeagal argument");
        }
        return project.getProjectType().getDir();
    }
}

