package cn.oyzh.easyshell.test;

import cn.oyzh.common.system.SystemUtil;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.IOException;

public class SSHTest {

    public static void main(String[] args) throws JSchException, IOException {
        SSHTest sshTest = new SSHTest();
        for (int i = 0; i < 20; i++) {
            sshTest.test1();
//            sshTest.test2();
        }
    }

    public void test1() throws JSchException {
        double m1 = SystemUtil.getUsedMemory();
        JSch jsch = new JSch();
        Session session = jsch.getSession("ssh", "127.0.0.1", 22);
        session.setPassword("123456");
        session.setConfig("StrictHostKeyChecking", "no");
//        session.setConfig("Compression", "yes");
//        session.setConfig("CompressionLevel", "9");
        session.connect();
        ChannelShell shell = (ChannelShell) session.openChannel("shell");
        shell.connect();
        session.disconnect();
        double m2 = SystemUtil.getUsedMemory();
        SystemUtil.gc();
        double m3 = SystemUtil.getUsedMemory();
        System.out.println("m1:" + m1);
        System.out.println("m2:" + m2);
        System.out.println("m3:" + m3);
        System.out.println("----------------------------");
    }

    public void test2() throws IOException {
        double m1 = SystemUtil.getUsedMemory();
        SSHClient sshClient = new SSHClient();
        // 允许连接不在known_hosts文件中的主机
        sshClient.addHostKeyVerifier(new PromiscuousVerifier());
        // 连接服务器
        sshClient.connect("127.0.0.1", 22);
        // 使用密码认证
        sshClient.authPassword("ssh", "123456");
        sshClient.useCompression();
        net.schmizz.sshj.connection.channel.direct.Session session = sshClient.startSession();
        net.schmizz.sshj.connection.channel.direct.Session.Shell shell = session.startShell();
        sshClient.disconnect();
        double m2 = SystemUtil.getUsedMemory();
        SystemUtil.gc();
        double m3 = SystemUtil.getUsedMemory();
        System.out.println("m1:" + m1);
        System.out.println("m2:" + m2);
        System.out.println("m3:" + m3);
        System.out.println("----------------------------");
    }
}
