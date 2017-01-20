/*
 * Decompiled with CFR 0_118.
 */
package org.tizen.tpklib.lib;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import org.tizen.tpklib.lib.PathUtil;

public class Log {
    private FileWriter writer = null;
    private FileWriter errorWriter = null;
    private String prefix = null;
    private int DEFAULT_ERROR_LEVEL = 1;
    private int MAX_STACK_LEVEL = 7;
    private final String LOG_TYPE_PRE_SEPERATOR = "[";
    private final String LOG_TYPE_POST_SEPERATOR = "]";
    private final String LOG_PREFIX_SEPERATOR = " ";
    private final String LOG_ERROR_FILE = "error.log";
    private static Throwable logException = null;

    private Log(File logFile) {
        try {
            this.writer = new FileWriter(logFile);
        }
        catch (IOException e) {
            logException = e;
            return;
        }
        String errorLogPath = PathUtil.addPath(logFile.getParent(), "error.log");
        File errorLogFile = new File(errorLogPath);
        if (errorLogFile.exists()) {
            errorLogFile.delete();
        }
        try {
            errorLogFile.createNewFile();
        }
        catch (IOException e) {
            logException = e;
            return;
        }
        try {
            this.errorWriter = new FileWriter(errorLogPath);
        }
        catch (IOException e) {
            logException = e;
            return;
        }
    }

    public void setErrorLevel(int level) {
        this.DEFAULT_ERROR_LEVEL = level <= 0 ? 0 : (level >= 4 ? 4 : level);
    }

    public boolean log(String message) {
        return this.log(LogType.LOG, message);
    }

    public boolean info(String message) {
        return this.log(LogType.INFO, message);
    }

    public boolean warn(String message) {
        return this.log(LogType.WARN, message);
    }

    public boolean error(String message) {
        return this.log(LogType.ERROR, message);
    }

    public boolean exception(Throwable e) {
        boolean ret = this.log(LogType.EXCEPTION, e.getMessage());
        PrintWriter pw = new PrintWriter(this.writer);
        e.printStackTrace(pw);
        pw.flush();
        return ret;
    }

    public static String getCallerMethod(StackTraceElement[] stacks, int n) {
        if (stacks.length <= n) {
            return null;
        }
        return stacks[n].getClassName() + "." + stacks[n].getMethodName() + " : " + stacks[n].getLineNumber();
    }

    public static Throwable getException() {
        return logException;
    }

    private boolean log(LogType logType, String message) {
        if (this.writer == null) {
            return false;
        }
        StringBuffer logMessage = this.getLogMessage(logType, message);
        try {
            this.writer.write(logMessage.toString() + "\n");
            this.writer.flush();
        }
        catch (IOException e) {
            logException = e;
            return false;
        }
        if (logType.getLevel() <= this.DEFAULT_ERROR_LEVEL) {
            this.errorLog(logMessage);
        }
        return true;
    }

    private boolean errorLog(StringBuffer msg) {
        try {
            this.errorWriter.write(msg.toString() + "\n");
            this.errorWriter.flush();
            this.stackLog();
            return true;
        }
        catch (IOException e) {
            logException = e;
            return false;
        }
    }

    private void stackLog() {
        StackTraceElement[] stacks = new Throwable().getStackTrace();
        if (stacks.length <= 0) {
            return;
        }
        try {
            for (int i = 3; i < stacks.length; ++i) {
                this.writer.write("[stack]" + Log.getCallerMethod(stacks, i) + "\n");
                if (i <= this.MAX_STACK_LEVEL) {
                    continue;
                }
                break;
            }
        }
        catch (IOException e) {
            logException = e;
            return;
        }
    }

    private StringBuffer getLogMessage(LogType logType, String message) {
        StringBuffer sb = null;
        sb = message == null ? new StringBuffer("") : new StringBuffer(message);
        if (logType != null && logType != LogType.LOG) {
            sb.insert(0, "[" + logType.toString() + "]");
        }
        if (this.prefix != null && !this.prefix.isEmpty()) {
            sb.insert(0, this.prefix + " ");
        }
        return sb;
    }

    public void setPrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return;
        }
        this.prefix = prefix;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public static Log createLog(String logFilePath) {
        if (logFilePath == null || logFilePath.isEmpty()) {
            return null;
        }
        File logFile = new File(logFilePath);
        if (logFile.exists()) {
            if (logFile.isDirectory()) {
                return null;
            }
            try {
                logFile.delete();
            }
            catch (SecurityException e) {
                logException = e;
                return null;
            }
        }
        File parentFile = logFile.getParentFile();
        if (parentFile == null) {
            return null;
        }
        if (!parentFile.exists()) {
            try {
                parentFile.mkdirs();
            }
            catch (SecurityException e) {
                logException = e;
                return null;
            }
        }
        try {
            logFile.createNewFile();
            return new Log(logFile);
        }
        catch (SecurityException e) {
            logException = e;
            return null;
        }
        catch (IOException e) {
            logException = e;
            return null;
        }
        catch (Exception e) {
            logException = e;
            return null;
        }
    }

    public void closeLog() {
        if (this.errorWriter != null) {
            try {
                this.errorWriter.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (this.writer != null) {
            try {
                this.writer.close();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static enum LogType {
        LOG("", 4),
        INFO("info", 3),
        WARN("warn", 2),
        ERROR("error", 1),
        EXCEPTION("Excep", 0);
        
        private String logType = null;
        private int level;

        private LogType(String type, int level) {
            this.logType = type;
            this.level = level;
        }

        public int getLevel() {
            return this.level;
        }

        public String toString() {
            return this.logType;
        }
    }

}

