/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

public class PropertyParser {
    private static final String PROPERTY_SEPERATOR = "=";

    public static Properties parsing(String filePath) throws FileNotFoundException, IOException {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return PropertyParser.parsing(new File(filePath));
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static Properties parsing(File file) throws FileNotFoundException, IOException {
        Properties props;
        if (file == null || !file.exists()) {
            return null;
        }
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        String line = null;
        props = new Properties();
        try {
            while ((line = br.readLine()) != null) {
                props.setProperty(PropertyParser.getKey(line), PropertyParser.getValue(line));
            }
        }
        finally {
            if (br != null) {
                br.close();
            }
            if (fr != null) {
                fr.close();
            }
        }
        return props;
    }

    private static String getKey(String line) {
        int index = line.indexOf("=");
        return line.substring(0, index);
    }

    private static String getValue(String line) {
        int index = line.indexOf("=");
        return line.substring(index + 1);
    }
}

