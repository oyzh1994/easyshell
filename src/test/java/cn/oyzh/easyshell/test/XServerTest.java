package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.x11.ShellX11Manager;
import org.junit.Test;

public class XServerTest {

    @Test
    public void test1() {
        ShellX11Manager.startXServer();
        System.out.println("1111");
    }
}
