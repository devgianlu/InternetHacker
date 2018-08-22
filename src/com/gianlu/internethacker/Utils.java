package com.gianlu.internethacker;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.OutputStream;

public class Utils {

    /**
     * Split a string without using Regex
     */
    @NotNull
    public static String[] split(@NotNull String str, char c) {
        int size = 1;
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) size++;
        }

        String tmp = str;
        String[] split = new String[size];
        for (int j = size - 1; j >= 0; j--) {
            int i = tmp.lastIndexOf(c);
            if (i == -1) {
                split[j] = tmp;
            } else {
                split[j] = tmp.substring(i + 1, tmp.length());
                tmp = tmp.substring(0, i);
            }
        }

        return split;
    }

    public static void putShort(OutputStream out, short val) throws IOException {
        out.write((val >>> 8) & 0xFF);
        out.write(val & 0xFF);
    }

    public static void putInt(OutputStream out, int val) throws IOException {
        out.write((val >>> 24) & 0xFF);
        out.write((val >>> 16) & 0xFF);
        out.write((val >>> 8) & 0xFF);
        out.write(val & 0xFF);
    }
}
