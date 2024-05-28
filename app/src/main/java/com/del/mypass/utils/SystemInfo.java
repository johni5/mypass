package com.del.mypass.utils;

import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Created by DodolinEL
 * date: 22.05.2024
 */
public class SystemInfo {

    private String nameOS;
    private String serialNumberOS;
    private String serialNumberBaseboard;
    private String mac;

    public SystemInfo() {
        try {
            init();
            Utils.getLogger().info(toString());
        } catch (Exception e) {
            Utils.getLogger().error(e.getMessage(), e);
        }
    }

    interface ScannerHandler {

        void handle(Scanner scanner);

    }

    private String cmdResult(Process process) throws IOException {
        StringBuilder r = new StringBuilder();
        cmdResult(process, s -> {
            while (s.hasNext()) {
                String next = s.next();
                if (next != null) {
                    if (r.length() > 0) r.append(" ");
                    r.append(next.trim());
                }
            }
        });
        return r.toString();
    }

    private void cmdResult(Process process, ScannerHandler handler) throws IOException {
        OutputStream os = process.getOutputStream();
        try (InputStream is = process.getInputStream()) {
            os.close();
            Scanner sc = new Scanner(is); // IBM866 to russian
            handler.handle(sc);
        }
    }

    private void initWin32() throws IOException {
        String s = cmdResult(
                Runtime.getRuntime().exec(new String[]{"wmic", "os", "get", "serialnumber"})
        );
        List<String> res = Lists.newArrayList(s.split(" "));
        if (res.size() > 1) serialNumberOS = res.get(1);

        s = cmdResult(
                Runtime.getRuntime().exec(new String[]{"wmic", "baseboard", "get", "serialnumber"})
        );
        res = Lists.newArrayList(s.split(" "));
        if (res.size() > 1) serialNumberBaseboard = res.get(1);

        s = cmdResult(
                Runtime.getRuntime().exec(new String[]{"getmac", "/fo", "csv", "/nh", "/v"})
        );
        res = Lists.newArrayList(s.
                replace("\" \"", "\",\"").
                split(",")).stream().
                map(ss -> ss.replace("\"", "")).
                collect(Collectors.toList());
        if (res.size() > 2) mac = res.get(2);
    }

    private void initLinux() throws IOException {
        String s = cmdResult(
                Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "sudo dmidecode -s system-uuid"})
        );
        List<String> res = Lists.newArrayList(s.split(" "));
        if (res.size() > 0) serialNumberOS = res.get(0);

        s = cmdResult(
                Runtime.getRuntime().exec(new String[]{"/bin/bash", "-c", "sudo dmidecode -s baseboard-serial-number"})
        );
        res = Lists.newArrayList(s.split(" "));
        if (res.size() > 0) serialNumberBaseboard = res.get(0);

        cmdResult(Runtime.getRuntime().exec(new String[]{"ifconfig"}), scanner -> {
            StringBuilder r = new StringBuilder();
            while (scanner.hasNext()) {
                String next = scanner.next();
                if (next != null && next.equalsIgnoreCase("hwaddr") && scanner.hasNext()) {
                    r.append(scanner.next());
                    break;
                }
            }
            mac = r.toString();
        });
    }

    private void initMacOS() {

    }

    private void init() {
        nameOS = System.getProperty("os.name");

        serialNumberOS = "-";
        serialNumberBaseboard = "-";
        mac = "-";
        try {
            if (nameOS.startsWith("Windows")) {
                initWin32();
            } else if (nameOS.startsWith("Linux")) {
                initLinux();
            } else if (nameOS.startsWith("Mac")) {
                initMacOS();
            }
        } catch (Exception e) {
            Utils.getLogger().error(e.getMessage(), e);
        }
    }

    public String getSerialNumberOS() {
        return serialNumberOS;
    }

    public String getSerialNumberBaseboard() {
        return serialNumberBaseboard;
    }

    public String getMac() {
        return mac;
    }

    public String getNameOS() {
        return nameOS;
    }

    @Override
    public String toString() {
        return String.format(
                "nameOS=%s, serialNumberOS=%s, serialNumberBaseboard=%s, mac=%s",
                nameOS, serialNumberOS, serialNumberBaseboard, mac
        );
    }
}
