package com.del.mypass.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

/**
 * Created by DodolinEL
 * date: 02.07.2019
 */
final public class FileUtils {

    public static byte[] readFile(File file) throws IOException {
        ReadableByteChannel src = Channels.newChannel(new FileInputStream(file));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        WritableByteChannel dest = Channels.newChannel(out);
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        transfer(src, dest, buffer);
        return out.toByteArray();
    }

    public static void writeToFile(File file, byte[] data) throws IOException {
        ReadableByteChannel src = Channels.newChannel(new ByteArrayInputStream(data));
        WritableByteChannel dest = Channels.newChannel(new FileOutputStream(file));
        ByteBuffer buffer = ByteBuffer.allocateDirect(16 * 1024);
        transfer(src, dest, buffer);
    }

    private static void transfer(ReadableByteChannel src,
                                 WritableByteChannel dest,
                                 ByteBuffer buffer) throws IOException {
        try {
            while (src.read(buffer) != -1) {
                buffer.flip();
                dest.write(buffer);
                buffer.compact();
            }
            buffer.flip();
            while (buffer.hasRemaining()) {
                dest.write(buffer);
            }
        } finally {
            safeClose(dest);
            safeClose(src);
        }
    }

    public static void safeClose(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                //
            }
        }
    }

}
