/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib.zip;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.List;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.tizen.tpklib.lib.PathUtil;
import org.tizen.tpklib.lib.zip.AbstractZip;

public class ZipLibrary
extends AbstractZip {
    static final int BUFFER = 4096;

    public File extract(File zipFile, File dstDir, String charset) throws IOException {
        return this.extract(zipFile, null, dstDir, charset);
    }

    public File extract(File zipFile, List<String> fileListToExtract, File dstDir, String charset) throws IOException {
        if (zipFile == null || !zipFile.exists()) {
            throw new FileNotFoundException(zipFile.toString());
        }
        if (dstDir == null) {
            dstDir = zipFile.getParentFile();
        }
        if (charset == null) {
            charset = Charset.defaultCharset().name();
        }
        if (dstDir.exists() && dstDir.isFile()) {
            throw new IOException("Cannot create destination directory: " + dstDir);
        }
        if (!dstDir.exists()) {
            dstDir.mkdirs();
        }
        ZipArchiveInputStream zis = new ZipArchiveInputStream(new FileInputStream(zipFile), charset, false);
        Throwable throwable = null;
        try {
            ZipArchiveEntry entry = null;
            while ((entry = zis.getNextZipEntry()) != null) {
                String name = entry.getName();
                if (fileListToExtract != null && !fileListToExtract.contains(name)) continue;
                File targetFile = new File(dstDir, name);
                if (entry.isDirectory()) {
                    targetFile.mkdirs();
                    continue;
                }
                targetFile.createNewFile();
                BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(targetFile));
                Throwable throwable2 = null;
                try {
                    int readBuf = 0;
                    byte[] buf = new byte[4096];
                    while ((readBuf = zis.read(buf)) >= 0) {
                        bos.write(buf, 0, readBuf);
                    }
                    continue;
                }
                catch (Throwable readBuf) {
                    throwable2 = readBuf;
                    throw readBuf;
                }
                finally {
                    if (bos == null) continue;
                    if (throwable2 != null) {
                        try {
                            bos.close();
                        }
                        catch (Throwable readBuf) {
                            throwable2.addSuppressed(readBuf);
                        }
                        continue;
                    }
                    bos.close();
                    continue;
                }
            }
        }
        catch (Throwable entry) {
            throwable = entry;
            throw entry;
        }
        finally {
            if (zis != null) {
                if (throwable != null) {
                    try {
                        zis.close();
                    }
                    catch (Throwable entry) {
                        throwable.addSuppressed(entry);
                    }
                } else {
                    zis.close();
                }
            }
        }
        return dstDir;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    @Override
    public boolean zipping(File file, String outFilePath, int permission) throws FileNotFoundException, IOException {
        if (file == null || outFilePath == null) {
            return false;
        }
        File[] zipFileList = file.listFiles();
        File outFile = new File(outFilePath);
        if (outFile.exists()) {
            throw new IOException("Already exist: " + outFilePath);
        }
        FileOutputStream dest = null;
        ZipArchiveOutputStream out = null;
        try {
            dest = new FileOutputStream(outFilePath);
            out = new ZipArchiveOutputStream(dest);
            boolean bl = this.zipping(zipFileList, out, file.getCanonicalPath(), permission);
            return bl;
        }
        finally {
            if (out != null) {
                try {
                    out.close();
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean zipping(File file, ZipArchiveOutputStream out, String rootPath, int permission) throws IOException {
        FileInputStream fInputStream = null;
        BufferedInputStream bInputStream = null;
        byte[] data = new byte[4096];
        if (file.listFiles() == null) {
            return true;
        }
        if (out == null || rootPath == null) {
            return false;
        }
        ZipArchiveEntry zipEntry = null;
        try {
            for (File f : file.listFiles()) {
                String relativePath = PathUtil.getRelativePath(rootPath, f.getCanonicalPath());
                if (f.isDirectory()) {
                    relativePath = relativePath + File.separator;
                }
                zipEntry = new ZipArchiveEntry(relativePath);
                if (permission == 0) {
                    permission = 493;
                }
                zipEntry.setUnixMode(permission);
                if (f.isDirectory()) {
                    out.putArchiveEntry(zipEntry);
                    out.closeArchiveEntry();
                    this.printAddingMessage(zipEntry);
                    this.zipping(f, out, rootPath, permission);
                    continue;
                }
                out.putArchiveEntry(zipEntry);
                fInputStream = new FileInputStream(f);
                bInputStream = new BufferedInputStream(fInputStream, 4096);
                int readBuf = 0;
                while ((readBuf = bInputStream.read(data)) > 0) {
                    out.write(data, 0, readBuf);
                }
                out.flush();
                this.printAddingMessage(zipEntry);
                if (bInputStream != null) {
                    bInputStream.close();
                }
                if (fInputStream != null) {
                    fInputStream.close();
                }
                if (zipEntry == null) continue;
                out.closeArchiveEntry();
            }
        }
        finally {
            if (bInputStream != null) {
                bInputStream.close();
            }
            if (fInputStream != null) {
                fInputStream.close();
            }
            if (zipEntry != null) {
                try {
                    out.closeArchiveEntry();
                }
                catch (IOException var9_10) {}
            }
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private boolean zipping(File[] fileList, ZipArchiveOutputStream out, String rootPath, int permission) throws IOException {
        FileInputStream fInputStream = null;
        BufferedInputStream bInputStream = null;
        byte[] data = new byte[4096];
        if (fileList == null) {
            return true;
        }
        if (out == null || rootPath == null) {
            return false;
        }
        ZipArchiveEntry zipEntry = null;
        try {
            for (File f : fileList) {
                String relativePath = PathUtil.getRelativePath(rootPath, f.getCanonicalPath());
                if (f.isDirectory()) {
                    relativePath = relativePath + File.separator;
                }
                zipEntry = new ZipArchiveEntry(relativePath);
                if (permission == 0) {
                    permission = 493;
                }
                zipEntry.setUnixMode(permission);
                if (f.isDirectory()) {
                    out.putArchiveEntry(zipEntry);
                    out.closeArchiveEntry();
                    this.printAddingMessage(zipEntry);
                    this.zipping(f, out, rootPath, permission);
                    continue;
                }
                out.putArchiveEntry(zipEntry);
                fInputStream = new FileInputStream(f);
                bInputStream = new BufferedInputStream(fInputStream, 4096);
                int readBuf = 0;
                while ((readBuf = bInputStream.read(data)) > 0) {
                    out.write(data, 0, readBuf);
                }
                out.flush();
                this.printAddingMessage(zipEntry);
                if (bInputStream != null) {
                    bInputStream.close();
                }
                if (fInputStream != null) {
                    fInputStream.close();
                }
                if (zipEntry == null) continue;
                out.closeArchiveEntry();
            }
        }
        finally {
            if (bInputStream != null) {
                bInputStream.close();
            }
            if (fInputStream != null) {
                fInputStream.close();
            }
            if (zipEntry != null) {
                try {
                    out.closeArchiveEntry();
                }
                catch (IOException var9_10) {}
            }
        }
        return true;
    }

    private void printAddingMessage(ZipArchiveEntry entry) throws IOException {
        if (this.outStream == null || entry == null) {
            return;
        }
        long size = entry.getSize();
        String comment = null;
        comment = entry.isDirectory() ? "directory" : "file";
        this.printStream(entry.getName(), size, comment);
    }
}

