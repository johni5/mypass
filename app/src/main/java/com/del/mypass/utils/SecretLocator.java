package com.del.mypass.utils;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class SecretLocator {

    final private static String LINUX_PATH = "/enter.mps";
    final private static String WIN32_PATH = "\\enter.mps";

    private String privateKey;
    private Cache<String, String> fileCache;
    private String manualPath;

    public SecretLocator() {
        fileCache = CacheBuilder.newBuilder().expireAfterWrite(5, TimeUnit.MINUTES).build();
    }

    public String read() {
        return Utils.nvl(loadSystemKey(), "") + "_" + Utils.nvl(getPrivateKey(), "");
    }

    public String getManualPath() {
        return manualPath;
    }

    public String getPath() {
        if (StringUtil.isTrimmedEmpty(manualPath)) {
            if (isLinux()) {
                return System.getProperty("user.home") + LINUX_PATH;
            } else if (isWindows()) {
                return System.getProperty("user.home") + WIN32_PATH;
            }
        }
        return manualPath;
    }

    public void setManualPath(String manualPath) {
        this.manualPath = manualPath;
    }

    private String loadSystemKey() {
        String fName = getPath();
        try {
            return fileCache.get(fName, () -> {
                File f = new File(fName);
                if (f.exists()) {
                    byte[] data = FileUtils.readFile(f);
                    if (data != null && data.length > 0) {
                        return new String(data, "UTF-8");
                    }
                }
                throw new CommonException("Enter file not found");
            });
        } catch (Exception e) {
            Utils.getLogger().error(e.getMessage(), e);
        }
        return "";
    }

    private boolean isWindows() {
        return System.getProperty("os.name").startsWith("Windows");
    }

    private boolean isLinux() {
        return System.getProperty("os.name").startsWith("Linux");
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }
}
