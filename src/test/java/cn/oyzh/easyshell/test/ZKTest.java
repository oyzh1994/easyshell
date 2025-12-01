package cn.oyzh.easyshell.test;

import org.apache.zookeeper.client.FourLetterWordMain;
import org.apache.zookeeper.common.X509Exception;
import org.junit.Test;

import java.io.IOException;

/**
 * @author oyzh
 * @since 2024-11-20
 */
public class ZKTest {

    // private int port = 2181;
    // private int port = 52181;
    // private int port = 32181;
    // private String host = "192.168.3.4";
//    private String host = "192.168.2.19";
    private String host = "192.168.2.4";
    private int port = 32181;
    // private String host = "localhost";
    // private int port = 12184;

    @Test
    public void test1() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "conf");
        System.out.println(result);
    }

    @Test
    public void test2() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "envi");
        System.out.println(result);
    }

    @Test
    public void test3() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "mntr");
        System.out.println(result);
    }

    @Test
    public void test4() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "ruok");
        System.out.println(result);
    }

    @Test
    public void test5() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "srvr");
        System.out.println(result);
    }

    @Test
    public void test6() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "stat");
        System.out.println(result);
    }

    @Test
    public void test7() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "wchs");
        System.out.println(result);
    }

    @Test
    public void test8() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "wchc");
        System.out.println(result);
    }

    @Test
    public void test9() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "wchp");
        System.out.println(result);
    }

    @Test
    public void test10() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "dump");
        System.out.println(result);
    }

    @Test
    public void test11() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "crst");
        System.out.println(result);
    }

    @Test
    public void test12() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "srst");
        System.out.println(result);
    }

    @Test
    public void test13() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "cons");
        System.out.println(result);
    }

    @Test
    public void test14() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "reqs");
        System.out.println(result);
    }

    @Test
    public void test15() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "kill");
        System.out.println(result);
    }

    @Test
    public void test16() throws X509Exception.SSLContextException, IOException {
        String result = FourLetterWordMain.send4LetterWord(host, port, "dirs");
        System.out.println(result);
    }
}
