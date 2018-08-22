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

    /**
     * Writes a {@link Short} into an {@link OutputStream}
     */
    public static void putShort(OutputStream out, short val) throws IOException {
        out.write((val >>> 8) & 0xFF);
        out.write(val & 0xFF);
    }

    /**
     * Writes an {@link Integer} into an {@link OutputStream}
     */
    public static void putInt(OutputStream out, int val) throws IOException {
        out.write((val >>> 24) & 0xFF);
        out.write((val >>> 16) & 0xFF);
        out.write((val >>> 8) & 0xFF);
        out.write(val & 0xFF);
    }

    /**
     * Returns a binary representation of the given bytes array. Used mainly for debugging.
     *
     * @return a binary representation of {@param b}
     */
    @NotNull
    public static String toBitString(byte[] b) {
        final char[] bits = new char[8 * b.length];
        for (int i = 0; i < b.length; i++) {
            final byte byteval = b[i];
            int bytei = i << 3;
            int mask = 0x1;
            for (int j = 7; j >= 0; j--) {
                final int bitval = byteval & mask;
                if (bitval == 0) {
                    bits[bytei + j] = '0';
                } else {
                    bits[bytei + j] = '1';
                }
                mask <<= 1;
            }
        }
        return String.valueOf(bits);
    }
}
