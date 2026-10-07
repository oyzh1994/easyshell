package cn.oyzh.easyshell.test;

import cn.oyzh.easyshell.x11.ShellX11Manager;
import org.junit.Test;

/**
 * 测试启动 X Server 的测试类
 *
 * @author oyzh
 * @since 2025-03-08
 */
public class XServerTest {

    // 启动 X Server
    @Test
    public void test1() {
        ShellX11Manager.startXServer();
        System.out.println("1111");
    }
}
