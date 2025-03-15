package cn.oyzh.easyssh.test;

import cn.oyzh.easyshell.x11.X11Manager;
import org.junit.Test;

public class XServerTest {

    @Test
    public void test1() {
        X11Manager.startXServer();
        System.out.println("1111");
    }
}
