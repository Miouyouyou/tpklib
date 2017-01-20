/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

public class CommandRunner {
    private static Process process = null;
    private long startTime = 0;
    private long endTime = 0;
    public static final int CMD_ERROR = -1;
    public static final int CMD_RUNNING = -2;
    private int exitValue = -2;

    public boolean execute(List<String> cmd, String runningDir, Map<String, String> env, boolean waitable) throws IOException, InterruptedException {
        File runningDirFile;
        if (cmd == null || cmd.isEmpty()) {
            this.exitValue = -1;
            return false;
        }
        ProcessBuilder pb = new ProcessBuilder(cmd);
        if (runningDir != null && !runningDir.isEmpty() && (runningDirFile = new File(runningDir)).exists()) {
            pb.directory(runningDirFile);
        }
        if (env != null) {
            Map<String, String> pbEnv = pb.environment();
            pbEnv.putAll(env);
        }
        this.startTime = System.currentTimeMillis();
        process = pb.start();
        if (process == null) {
            return false;
        }
        return true;
    }

    public long getProgressTime() {
        if (process == null) {
            return 0;
        }
        if (this.isAlive()) {
            this.endTime = System.currentTimeMillis();
            return this.endTime - this.startTime;
        }
        if (this.endTime == 0) {
            this.endTime = System.currentTimeMillis();
            return this.endTime - this.startTime;
        }
        return this.endTime - this.startTime;
    }

    public InputStream getInputStream() {
        return process.getInputStream();
    }

    public OutputStream getOuputStream() {
        return process.getOutputStream();
    }

    public InputStream getErrorStream() {
        return process.getErrorStream();
    }

    public int getExitValue() {
        this.exitValue = this.isAlive() ? -2 : process.exitValue();
        return this.exitValue;
    }

    public void destroyProcess() {
        if (this.isAlive()) {
            this.exitValue = -1;
            process.destroy();
        }
    }

    public boolean isAlive() {
        try {
            process.exitValue();
            return false;
        }
        catch (IllegalThreadStateException e) {
            return true;
        }
    }

    public int waitFor() throws InterruptedException {
        this.exitValue = process.waitFor();
        return this.exitValue;
    }

    public void waitProcess(long milliSeconds) throws InterruptedException {
        process.wait(milliSeconds);
    }

    public Process getProcess() {
        return process;
    }

    public static Process getCurrentProcess() {
        return process;
    }
}

