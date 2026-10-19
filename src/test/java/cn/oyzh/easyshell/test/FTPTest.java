package cn.oyzh.easyshell.test;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.junit.Test;

import java.io.IOException;

/**
 * FTP 连接与目录列表功能的测试
 *
 * @author oyzh
 * @since 2025-04-25
 */
public class FTPTest {

    @Test
    public void test() throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.connect("127.0.0.1",21);
        ftp.login("mobaxterm","123456");
        FTPFile[] files= ftp.listDirectories();
        for (FTPFile file : files) {
            System.out.println(file.getName());
            System.out.println(file.getGroup());
        }
    }

    @Test
    public void test1() throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.connect("192.168.22.141",21);
        ftp.login("root","123456");
        FTPFile[] files= ftp.listDirectories();
        for (FTPFile file : files) {
            System.out.println(file.getName());
            System.out.println(file.getGroup());
        }
    }

    @Test
    public void test2() throws IOException {
        FTPClient ftp = new FTPClient();
        ftp.connect("192.168.3.156",21);
        ftp.login("root","123456");
        FTPFile[] files= ftp.listDirectories();
        for (FTPFile file : files) {
            System.out.println(file.getName());
            System.out.println(file.getGroup());
        }
    }
}
