package com.del.mypass.utils;

import com.google.common.collect.Lists;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtil {

    private StringUtil() {
    }

    public static String safeToString(Object src, String def) {
        return src == null ? def : src.toString();
    }

    public static boolean isTrimmedEmpty(Object s) {
        return s == null || s.toString().trim().length() == 0;
    }

    public static String firstNotEmpty(Object... s) {
        for (Object o : s) {
            if (!isTrimmedEmpty(o)) {
                return o.toString();
            }
        }
        return null;
    }

    public static boolean isAllTrimmedEmpty(Object... s) {
        if (s == null) {
            return true;
        }
        for (Object o : s) {
            if (!isTrimmedEmpty(o)) {
                return false;
            }
        }
        return true;
    }

    public static String normalizeLine(Object text) {
        if (text != null) {
            return text.toString().replace("\n", " ").replace("\r", " ").trim();
        }
        return "";
    }

    public static String wrapIfNotEmpty(Object value, String wrapper) {
        if (!isTrimmedEmpty(value)) {
            return wrapper + value + wrapper;
        }
        return null;
    }

    public static String wrapIfNotNull(Object value, String wrapper) {
        if (value != null) {
            return wrapper + value.toString() + wrapper;
        }
        return null;
    }

    public static String scrapMiddle(String str, int len) {
        return scrapMiddle(str, len, "...");
    }

    public static String scrapMiddle(String str, int len, boolean repeat) {
        return scrapMiddle(str, len, "...", repeat);
    }

    public static String scrapMiddle(String str, int len, String middleStr) {
        return scrapMiddle(str, len, middleStr, true);
    }

    public static String scrapMiddle(String str, int len, String middleStr, boolean repeat) {
        if (str == null || str.length() <= len) {
            return str;
        }
        int substrLen = len - Math.round((float) (middleStr.length() / 2));
        int substrLen2 = str.length() - substrLen;
        if (!repeat && (substrLen2 < substrLen)) {
            if (str.length() < substrLen * 2) {
                return str;
            }
            substrLen2 = substrLen + 1;
        }
        substrLen2 = Math.max(substrLen2, substrLen);
        String scrapText = str.substring(0, substrLen) + middleStr + str.substring(substrLen2);
        return scrapText.length() > str.length() ? str : scrapText;
    }

    public static List<Integer> extractNumbers(String text) {
        if (isTrimmedEmpty(text)) return null;
        Pattern p = Pattern.compile("-?\\d+");
        Matcher m = p.matcher(text);
        List<Integer> i = Lists.newArrayList();
        while (m.find()) {
            try {
                i.add(Integer.parseInt(m.group()));
            } catch (NumberFormatException e) {
                //
            }
        }
        return i;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }


}