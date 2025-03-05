//package cn.oyzh.easyssh.ssh;
//
//import com.jcraft.jsch.ChannelExec;
//import com.jcraft.jsch.JSchException;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.InputStreamReader;
//import java.io.OutputStream;
//
///**
// * @author oyzh
// * @since 2023/8/16
// */
//public class SSHExec {
//
//    private ChannelExec channel;
//
//    public SSHExec(ChannelExec channel) {
//        this.channel = channel;
//    }
//
//    public void close() {
//        try {
//            this.channel.disconnect();
//            this.channel = null;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public boolean isClosed() {
//        return this.channel == null || this.channel.isClosed();
//    }
//
//    public void setPtySize(int columns, int rows, int sizeW, int sizeH) {
//        this.channel.setPtySize(columns, rows, sizeW, sizeH);
//    }
//
//    public InputStream getInputStream() throws IOException {
//        return this.channel.getInputStream();
//    }
//
//    public OutputStream getOutputStream() throws IOException {
//        return this.channel.getOutputStream();
//    }
//
//    public void connect(int connectTimeout) throws JSchException {
//        this.channel.connect(connectTimeout);
//    }
//
//    public boolean isConnected() {
//        return this.channel.isConnected();
//    }
//
//    public String exec(String cmd) throws IOException, JSchException {
//        this.channel.setCommand(cmd);
////            this.channel.connect(3000);
//        InputStream in = this.channel.getInputStream();
//        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
//        String line;
//        StringBuilder sb = new StringBuilder();
//        while ((line = reader.readLine()) != null) {
//            sb.append(line);
//        }
//        this.channel.disconnect();
//        return sb.toString();
//    }
//
//    public String id_gn(int uid) throws JSchException, IOException {
//        return exec("/usr/bin/id -gn " + uid);
//    }
//
//    public String id_un(int uid) throws JSchException, IOException {
//        return exec("/usr/bin/id -un " + uid);
//    }
//
//    public String stat(String filePath) throws JSchException, IOException {
//        return exec("stat -c '%U %G' " + filePath);
//    }
//}
