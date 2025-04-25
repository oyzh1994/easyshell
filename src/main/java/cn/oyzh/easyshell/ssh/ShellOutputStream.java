//package cn.oyzh.easyshell.shell;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.nio.charset.StandardCharsets;
//
///**
// * @author oyzh
// * @since 2025-03-24
// */
//public class ShellOutputStream extends OutputStream {
//
//    private final OutputStream out = System.out;
//
//    @Override
//    public void write(byte[] b, int off, int len) throws IOException {
//       byte[] buffer = new String(b).getBytes(StandardCharsets.UTF_8);
//       super.write(buffer, off, len);
//    }
//
//    @Override
//    public void write(int b) throws IOException {
//        out.write(b);
//    }
//}
