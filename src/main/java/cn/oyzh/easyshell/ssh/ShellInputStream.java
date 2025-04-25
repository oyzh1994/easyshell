//package cn.oyzh.easyshell.shell;
//
//import org.jetbrains.annotations.NotNull;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.nio.charset.Charset;
//import java.nio.charset.StandardCharsets;
//import java.util.Arrays;
//
///**
// * @author oyzh
// * @since 2025-03-24
// */
//public class ShellInputStream extends InputStream {
//
//    private final InputStream in = System.in;
//
//    @Override
//    public int read() throws IOException {
//        return in.read();
//    }
//
//    @Override
//    public int read(byte[] b, int off, int len) throws IOException {
//        byte[] buffer = new byte[len];
//        int len1 = in.read(buffer, off, len);
//        buffer = new String(buffer).getBytes(StandardCharsets.UTF_8);
//        System.arraycopy(buffer, 0, b, 0, buffer.length);
//        return len1;
//    }
//}
