package com.liberaid.wavevisualizer;

import org.jetbrains.annotations.NotNull;

public final class Utils {
    private Utils() {}

    public static int bytesToIntLittleEndian(@NotNull byte[] bytes, int from) {
        int a = bytes[from];
        int b = bytes[from + 1];
        int c = bytes[from + 2];
        int d = bytes[from + 3];

        return (d << 24) | (c << 16) | (b << 8) | a;
    }

    public static int bytesToShortLittleEndian(@NotNull byte[] bytes, int from) {
        int a = bytes[from];
        int b = bytes[from + 1];

        return (b << 8) | a;
    }
}
