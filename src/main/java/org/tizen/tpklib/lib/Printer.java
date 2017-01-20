/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;

public class Printer {
    public static Thread printFromInput(final InputStream inputStream, final OutputStream outputStream) {
        if (inputStream == null || outputStream == null) {
            return null;
        }
        Thread printThread = new Thread(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                BufferedReader reader = null;
                String line = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(inputStream));
                    while ((line = reader.readLine()) != null) {
                        line = line + "\n";
                        outputStream.write(line.getBytes());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        printThread.start();
        return printThread;
    }

    public static void print(OutputStream outputStream, String message) throws IOException {
        if (outputStream != null) {
            outputStream.write(message.getBytes());
        }
    }

    public static void printCommandOutput(Process process, OutputStream outStream, OutputStream errStream) throws InterruptedException {
        Thread outputThread = null;
        if (outStream != null) {
            outputThread = Printer.print(process.getInputStream(), outStream);
        }
        Thread errorThread = null;
        if (errStream != null) {
            errorThread = Printer.print(process.getErrorStream(), errStream);
        }
        if (outputThread != null) {
            outputThread.join();
        }
        if (errorThread != null) {
            errorThread.join();
        }
    }

    private static Thread print(final InputStream input, final OutputStream output) {
        if (input == null || output == null) {
            return null;
        }
        Thread printThread = new Thread(new Runnable(){

            /*
             * WARNING - Removed try catching itself - possible behaviour change.
             */
            @Override
            public void run() {
                BufferedReader reader = null;
                String line = null;
                try {
                    reader = new BufferedReader(new InputStreamReader(input));
                    while ((line = reader.readLine()) != null) {
                        line = line + "\n";
                        output.write(line.getBytes());
                    }
                }
                catch (IOException e) {
                    e.printStackTrace();
                }
                finally {
                    if (reader != null) {
                        try {
                            reader.close();
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        printThread.start();
        return printThread;
    }

}

