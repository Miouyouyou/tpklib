/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XmlParser {
    public static Document parsing(String filePath) throws ParserConfigurationException, SAXException, IOException {
        if (filePath == null || filePath.isEmpty()) {
            return null;
        }
        return XmlParser.parsing(new File(filePath));
    }

    public static Document parsing(File filePath) throws ParserConfigurationException, SAXException, IOException {
        if (filePath == null || !filePath.exists()) {
            throw new FileNotFoundException(filePath.toString());
        }
        DocumentBuilderFactory domBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder domBuilder = domBuilderFactory.newDocumentBuilder();
        return domBuilder.parse(filePath);
    }
}

